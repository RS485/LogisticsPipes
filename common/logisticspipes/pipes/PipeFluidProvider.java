package logisticspipes.pipes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.interfaces.ISpecialTankHandler;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideFluids;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsFluidOrder;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class PipeFluidProvider extends FluidRoutedPipe implements IProvideFluids {

	public PipeFluidProvider(Item item) {
		super(item);
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (!getFluidOrderManager().hasOrders(ResourceType.PROVIDER) || !isNthTick(6)) {
			return;
		}

		LogisticsFluidOrder order = getFluidOrderManager().peekAtTopRequest(ResourceType.PROVIDER);
		int amountToSend, attemptedAmount;
		amountToSend = attemptedAmount = Math.min(order.getAmount(), 5000);
		for (Pair<TileEntity, ForgeDirection> pair : getAdjacentTanks(false)) {
			if (amountToSend <= 0) {
				break;
			}
			boolean fallback = true;
			if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if (handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					FluidStack drained = ((ISpecialTankAccessHandler) handler).drainFrom(pair.getValue1(), order.getFluid(), amountToSend, false);
					if (drained != null && drained.amount > 0 && order.getFluid().equals(FluidIdentifier.get(drained))) {
						drained = ((ISpecialTankAccessHandler) handler).drainFrom(pair.getValue1(), order.getFluid(), amountToSend, true);
						int amount = drained.amount;
						amountToSend -= amount;
						ItemIdentifierStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(drained);
						IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
						item.setDestination(order.getRouter().getSimpleID());
						item.setTransportMode(TransportMode.Active);
						this.queueRoutedItem(item, pair.getValue2());
						getFluidOrderManager().sendSuccessfull(amount, false, item);
						if (amountToSend <= 0) {
							break;
						}
					}
				}
			}
			if (fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler) pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				if (tanks != null) {
					for (FluidTankInfo tank : tanks) {
						if (tank == null) {
							continue;
						}
						FluidStack liquid;
						if ((liquid = tank.fluid) != null && liquid.getFluidID() != 0) {
							if (order.getFluid().equals(FluidIdentifier.get(liquid))) {
								int amount = Math.min(liquid.amount, amountToSend);
								FluidStack drained = ((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), amount, false);
								if (drained != null && drained.amount > 0  && order.getFluid().equals(FluidIdentifier.get(drained))) {
									drained = ((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), amount, true);
									while (drained.amount < amountToSend) {
										FluidStack addition = ((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), amountToSend - drained.amount, false);
										if (addition != null && addition.amount > 0  && order.getFluid().equals(FluidIdentifier.get(addition))) {
											addition = ((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), amountToSend - drained.amount, true);
											drained.amount += addition.amount;
										} else {
											break;
										}
									}
									amount = drained.amount;
									amountToSend -= amount;
									ItemIdentifierStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(drained);
									IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
									item.setDestination(order.getRouter().getSimpleID());
									item.setTransportMode(TransportMode.Active);
									this.queueRoutedItem(item, pair.getValue2());
									getFluidOrderManager().sendSuccessfull(amount, false, item);
									if (amountToSend <= 0) {
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		if (amountToSend >= attemptedAmount) {
			getFluidOrderManager().sendFailed();
		}
	}

	@Override
	public Map<FluidIdentifier, Integer> getAvailableFluids() {
		Map<FluidIdentifier, Integer> map = new HashMap<FluidIdentifier, Integer>();
		for (Pair<TileEntity, ForgeDirection> pair : getAdjacentTanks(false)) {
			boolean fallback = true;
			if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if (handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					Map<FluidIdentifier, Long> tmp = ((ISpecialTankAccessHandler) handler).getAvailableLiquid(pair.getValue1());
					for (Entry<FluidIdentifier, Long> entry : tmp.entrySet()) {
						if (map.containsKey(entry.getKey())) {
							long addition = ((long) map.get(entry.getKey())) + entry.getValue();
							map.put(entry.getKey(), addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
						} else {
							map.put(entry.getKey(), entry.getValue() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) (long) entry.getValue());
						}
					}
				}
			}
			if (fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler) pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				if (tanks != null) {
					for (FluidTankInfo tank : tanks) {
						if (tank == null) {
							continue;
						}
						FluidStack liquid;
						if ((liquid = tank.fluid) != null && liquid.getFluidID() != 0) {
							FluidIdentifier ident = FluidIdentifier.get(liquid);
							if (((IFluidHandler) pair.getValue1()).canDrain(pair.getValue2().getOpposite(), liquid.getFluid())) {
								if (((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), 1, false) != null) {
									if (map.containsKey(ident)) {
										long addition = ((long) map.get(ident)) + tank.fluid.amount;
										map.put(ident, addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
									} else {
										map.put(ident, tank.fluid.amount);
									}
								}
							}
						}
					}
				}
			}
		}
		Map<FluidIdentifier, Integer> result = new HashMap<FluidIdentifier, Integer>();
		//Reduce what has been reserved, add.
		for (Entry<FluidIdentifier, Integer> fluid : map.entrySet()) {
			int remaining = fluid.getValue() - getFluidOrderManager().totalFluidsCountInOrders(fluid.getKey());
			if (remaining < 1) {
				continue;
			}
			result.put(fluid.getKey(), remaining);
		}
		return result;
	}

	@Override
	public boolean disconnectPipe(TileEntity tile, ForgeDirection dir) {
		return SimpleServiceLocator.pipeInformationManager.isFluidPipe(tile);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_PROVIDER;
	}

	@Override
	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filter) {
		if (tree.isDone()) {
			return;
		}
		if (!(tree.getRequestType() instanceof FluidResource)) {
			return;
		}
		FluidIdentifier fluid = ((FluidResource) tree.getRequestType()).getFluid();
		int containedAmount = 0;
		for (Pair<TileEntity, ForgeDirection> pair : getAdjacentTanks(false)) {
			boolean fallback = true;
			if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if (handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					Map<FluidIdentifier, Long> map = ((ISpecialTankAccessHandler) handler).getAvailableLiquid(pair.getValue1());
					if (map.containsKey(fluid)) {
						long addition = (containedAmount) + map.get(fluid);
						containedAmount = addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition;
					}
				}
			}
			if (fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler) pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				if (tanks != null) {
					for (FluidTankInfo tank : tanks) {
						if (tank == null) {
							continue;
						}
						FluidStack liquid;
						if ((liquid = tank.fluid) != null && liquid.getFluidID() != 0) {
							if (fluid.equals(FluidIdentifier.get(liquid))) {
								if (((IFluidHandler) pair.getValue1()).canDrain(pair.getValue2().getOpposite(), liquid.getFluid())) {
									if (((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), 1, false) != null) {
										long addition = ((long) containedAmount) + liquid.amount;
										containedAmount = addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition;
									}
								}
							}
						}
					}
				}
			}
		}
		FluidLogisticsPromise promise = new FluidLogisticsPromise();
		promise.liquid = fluid;
		promise.amount = Math.min(tree.getMissingAmount(), containedAmount - root.getAllPromissesFor(this, fluid.getItemIdentifier()));
		promise.sender = this;
		promise.type = ResourceType.PROVIDER;
		if (promise.amount > 0) {
			tree.addPromise(promise);
		}
	}

	@Override
	public IOrderInfoProvider fullFill(FluidLogisticsPromise promise, IRequestFluid destination, ResourceType type, IAdditionalTargetInformation info) {
		return getFluidOrderManager().addOrder(promise, destination, type, info);
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}

	@Override
	//work in progress, currently not active code.
	public Set<ItemIdentifier> getSpecificInterests() {
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for (Pair<TileEntity, ForgeDirection> pair : getAdjacentTanks(false)) {
			boolean fallback = true;
			if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if (handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					Map<FluidIdentifier, Long> map = ((ISpecialTankAccessHandler) handler).getAvailableLiquid(pair.getValue1());
					for (FluidIdentifier ident : map.keySet()) {
						l1.add(ident.getItemIdentifier());
					}
				}
			}
			if (fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler) pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				if (tanks != null) {
					for (FluidTankInfo tank : tanks) {
						if (tank == null) {
							continue;
						}
						FluidStack liquid;
						if ((liquid = tank.fluid) != null && liquid.getFluidID() != 0) {
							if (((IFluidHandler) pair.getValue1()).canDrain(pair.getValue2().getOpposite(), liquid.getFluid())) {
								if (((IFluidHandler) pair.getValue1()).drain(pair.getValue2().getOpposite(), 1, false) != null) {
									FluidIdentifier ident = FluidIdentifier.get(liquid);
									l1.add(ident.getItemIdentifier());
								}
							}
						}
					}
				}
			}
		}
		return l1;
	}

	@Override
	public boolean canReceiveFluid() {
		return false;
	}
}
