package logisticspipes.pipes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.interfaces.ISpecialTankUtil;
import logisticspipes.interfaces.ITankUtil;
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
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Triplet;

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
		AtomicInteger amountToSend = new AtomicInteger();
		AtomicInteger attemptedAmount = new AtomicInteger();
		amountToSend.set(Math.min(order.getAmount(), 5000));
		attemptedAmount.set(Math.min(order.getAmount(), 5000));
		for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(false)) {
			if (amountToSend.get() <= 0) {
				break;
			}
			ITankUtil util = pair.getValue1();
			boolean fallback = true;
			if (util instanceof ISpecialTankUtil) {
				fallback = false;
				ISpecialTankAccessHandler handler = ((ISpecialTankUtil) util).getSpecialHandler();
				TileEntity tile = ((ISpecialTankUtil) util).getTileEntity();
				FluidStack drained = handler.drainFrom(tile, order.getFluid(), amountToSend.get(), false);
				if (drained != null && drained.amount > 0 && order.getFluid().equals(FluidIdentifier.get(drained))) {
					drained = handler.drainFrom(tile, order.getFluid(), amountToSend.get(), true);
					int amount = drained.amount;
					amountToSend.addAndGet(-amount);
					ItemIdentifierStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(FluidIdentifierStack.getFromStack(drained));
					IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
					item.setDestination(order.getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					this.queueRoutedItem(item, pair.getValue3());
					getFluidOrderManager().sendSuccessfull(amount, false, item);
					if (amountToSend.get() <= 0) {
						break;
					}
				}
			}
			if (fallback) {
				if (util.containsTanks()) {
					util.forEachFluid(fluidStack -> {
						if (amountToSend.get() <= 0) {
							return;
						}
						if (fluidStack.getFluid() != null) {
							if (order.getFluid().equals(fluidStack.getFluid())) {
								int amount = Math.min(fluidStack.getAmount(), amountToSend.get());
								FluidIdentifierStack drained = util.drain(amount, false);
								if (drained != null && drained.getAmount() > 0 && order.getFluid().equals(drained.getFluid())) {
									drained = util.drain(amount, true);
									while (drained.getAmount() < amountToSend.get()) {
										FluidIdentifierStack addition = util.drain(amountToSend.get() - drained.getAmount(), false);
										if (addition != null && addition.getAmount() > 0 && order.getFluid().equals(addition.getFluid())) {
											addition = util.drain(amountToSend.get() - drained.getAmount(), true);
											drained.raiseAmount(addition.getAmount());
										} else {
											break;
										}
									}
									amount = drained.getAmount();
									amountToSend.addAndGet(-amount);
									ItemIdentifierStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(drained);
									IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
									item.setDestination(order.getRouter().getSimpleID());
									item.setTransportMode(TransportMode.Active);
									queueRoutedItem(item, pair.getValue3());
									getFluidOrderManager().sendSuccessfull(amount, false, item);
								}
							}
						}
					});
				}
			}
		}
		if (amountToSend.get() >= attemptedAmount.get()) {
			getFluidOrderManager().sendFailed();
		}
	}

	@Override
	public Map<FluidIdentifier, Integer> getAvailableFluids() {
		Map<FluidIdentifier, Integer> map = new HashMap<>();
		for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(false)) {
			ITankUtil util = pair.getValue1();
			boolean fallback = true;
			if (util instanceof ISpecialTankUtil) {
				fallback = false;
				ISpecialTankAccessHandler handler = ((ISpecialTankUtil) util).getSpecialHandler();
				TileEntity tile = ((ISpecialTankUtil) util).getTileEntity();
				Map<FluidIdentifier, Long> tmp = handler.getAvailableLiquid(tile);
				for (Entry<FluidIdentifier, Long> entry : tmp.entrySet()) {
					if (map.containsKey(entry.getKey())) {
						long addition = ((long) map.get(entry.getKey())) + entry.getValue();
						map.put(entry.getKey(), addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
					} else {
						map.put(entry.getKey(), entry.getValue() > Integer.MAX_VALUE ? Integer.MAX_VALUE : entry.getValue().intValue());
					}
				}
			}
			if (fallback) {
				if (util.containsTanks()) {
					util.forEachFluid(liquid -> {
						if (liquid.getFluid() != null) {
							FluidIdentifier ident = liquid.getFluid();
							if (util.canDrain(ident)) {
								if (util.drain(ident.makeFluidIdentifierStack(1), false) != null) {
									if (map.containsKey(ident)) {
										long addition = ((long) map.get(ident)) + liquid.getAmount();
										map.put(ident, addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
									} else {
										map.put(ident, liquid.getAmount());
									}
								}
							}
						}
					});
				}
			}
		}
		Map<FluidIdentifier, Integer> result = new HashMap<>();
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
		AtomicInteger containedAmount = new AtomicInteger(0);
		for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(false)) {
			ITankUtil util = pair.getValue1();
			boolean fallback = true;
			if (util instanceof ISpecialTankUtil) {
				fallback = false;
				ISpecialTankAccessHandler handler = ((ISpecialTankUtil) util).getSpecialHandler();
				TileEntity tile = ((ISpecialTankUtil) util).getTileEntity();
				Map<FluidIdentifier, Long> map = handler.getAvailableLiquid(tile);
				if (map.containsKey(fluid)) {
					long addition = (containedAmount.get()) + map.get(fluid);
					containedAmount.set(addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
				}
			}
			if (fallback) {
				if (util.containsTanks()) {
					util.forEachFluid(liquid -> {
						if (liquid.getFluid() != null) {
							if (fluid.equals(liquid.getFluid())) {
								if (util.canDrain(liquid.getFluid())) {
									if (util.drain(liquid.getFluid().makeFluidIdentifierStack(1), false) != null) {
										long addition = ((long) containedAmount.get()) + liquid.getAmount();
										containedAmount.set(addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
									}
								}
							}
						}
					});
				}
			}
		}
		FluidLogisticsPromise promise = new FluidLogisticsPromise();
		promise.liquid = fluid;
		promise.amount = Math.min(tree.getMissingAmount(), containedAmount.get() - root.getAllPromissesFor(this, fluid.getItemIdentifier()));
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
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(false)) {
			ITankUtil util = pair.getValue1();
			boolean fallback = true;
			if (util instanceof ISpecialTankUtil) {
				fallback = false;
				ISpecialTankAccessHandler handler = ((ISpecialTankUtil) util).getSpecialHandler();
				TileEntity tile = ((ISpecialTankUtil) util).getTileEntity();
				handler.getAvailableLiquid(tile).keySet().stream()
						.map(FluidIdentifier::getItemIdentifier)
						.forEach(itemidCollection::add);
			}
			if (fallback) {
				if (util.containsTanks()) {
					util.forEachFluid(liquid -> {
						if (liquid.getFluid() != null) {
							if (util.canDrain(liquid.getFluid())) {
								if (util.drain(1, false) != null) {
									FluidIdentifier ident = liquid.getFluid();
									itemidCollection.add(ident.getItemIdentifier());
								}
							}
						}
					});
				}
			}
		}
	}

	@Override
	public boolean canReceiveFluid() {
		return false;
	}
}
