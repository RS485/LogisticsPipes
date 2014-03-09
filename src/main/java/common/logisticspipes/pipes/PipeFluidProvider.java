package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.interfaces.ISpecialTankHandler;
import logisticspipes.interfaces.routing.IFluidProvider;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.FluidRequestTreeNode;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.LogisticsFluidOrderManager;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class PipeFluidProvider extends FluidRoutedPipe implements IFluidProvider {
	
	LogisticsFluidOrderManager manager = new LogisticsFluidOrderManager();
	
	public PipeFluidProvider(int itemID) {
		super(itemID);
	}

	@Override
	public void enabledUpdateEntity() {
		if (!manager.hasOrders() || getWorld().getTotalWorldTime() % 6 != 0) return;
		
		Triplet<FluidIdentifier, Integer, IRequestFluid> order = manager.getFirst();
		int amountToSend, attemptedAmount;
		amountToSend = attemptedAmount = Math.min(order.getValue2(), 5000);
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			if(amountToSend <= 0) break;
			boolean fallback = true;
			if(SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if(handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					FluidStack drained = ((ISpecialTankAccessHandler)handler).drainFrom(pair.getValue1(), order.getValue1(), amountToSend, false);
					if(drained != null && order.getValue1() == FluidIdentifier.get(drained)) {
						drained = ((ISpecialTankAccessHandler)handler).drainFrom(pair.getValue1(), order.getValue1(), amountToSend, true);
						int amount = drained.amount;
						amountToSend -= amount;
						ItemStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(drained);
						IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, stack);
						item.setDestination(order.getValue3().getRouter().getSimpleID());
						item.setTransportMode(TransportMode.Active);
						this.queueRoutedItem(item, pair.getValue2());
						manager.sendAmount(amount);
						if(amountToSend <= 0) break;
					}
				}
			}
			if(fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				for(FluidTankInfo tank:tanks) {
					if(tank == null) continue;
					FluidStack liquid;
					if((liquid = tank.fluid) != null) {
						if(order.getValue1() == FluidIdentifier.get(liquid)) {
							int amount = Math.min(liquid.amount, amountToSend);
							FluidStack drained = ((IFluidHandler)pair.getValue1()).drain(pair.getValue2().getOpposite(), amount, false);
							if(drained != null && order.getValue1() == FluidIdentifier.get(drained)) {
								drained = ((IFluidHandler)pair.getValue1()).drain(pair.getValue2().getOpposite(), amount, true);
								amount = drained.amount;
								amountToSend -= amount;
								ItemStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidContainer(drained);
								IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, stack);
								item.setDestination(order.getValue3().getRouter().getSimpleID());
								item.setTransportMode(TransportMode.Active);
								this.queueRoutedItem(item, pair.getValue2());
								manager.sendAmount(amount);
								if(amountToSend <= 0) break;
							}
						}
					}
				}
			}
		}
		if(amountToSend >= attemptedAmount) {
			manager.sendFailed();
		}
	}

	@Override
	public Map<FluidIdentifier, Integer> getAvailableFluids() {
		Map<FluidIdentifier, Integer> map = new HashMap<FluidIdentifier, Integer>();
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			boolean fallback = true;
			if(SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if(handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					Map<FluidIdentifier, Long> tmp = ((ISpecialTankAccessHandler)handler).getAvailableLiquid(pair.getValue1());
					for(Entry<FluidIdentifier, Long> entry: tmp.entrySet()) {
						if(map.containsKey(entry.getKey())) {
							long addition = ((long)map.get(entry.getKey())) + entry.getValue();
							map.put(entry.getKey(), addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
						} else {
							map.put(entry.getKey(), entry.getValue() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(long)entry.getValue());
						}
					}
				}
			}
			if(fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				for(FluidTankInfo tank:tanks) {
					if(tank == null) continue;
					FluidStack liquid;
					if((liquid = tank.fluid) != null && liquid.fluidID != 0) {
						FluidIdentifier ident = FluidIdentifier.get(liquid);
						if(map.containsKey(ident)) {
							long addition = ((long)map.get(ident)) + tank.fluid.amount;
							map.put(ident, addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition);
						} else {
							map.put(ident, tank.fluid.amount);
						}
					}
				}
			}
		}
		//Reduce Ordered
		for(Triplet<FluidIdentifier, Integer, IRequestFluid> pair: manager.getAll()) {
			if(map.containsKey(pair.getValue1())) {
				int result = map.get(pair.getValue1()) - pair.getValue2();
				if(result > 0) {
					map.put(pair.getValue1(), result);
				} else {
					map.remove(pair.getValue1());
				}
			}
		}
		return map;
	}
	
	@Override
	public boolean disconnectPipe(TileEntity tile, ForgeDirection dir) {
		return tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe != null && ((TileGenericPipe)tile).pipe.transport instanceof PipeTransportFluids;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_PROVIDER;
	}

	@Override
	public void canProvide(FluidRequestTreeNode request, int donePromises) {
		if(request.isDone()) return;
		int containedAmount = 0;
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			boolean fallback = true;
			if(SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if(handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					Map<FluidIdentifier, Long> map = ((ISpecialTankAccessHandler)handler).getAvailableLiquid(pair.getValue1());
					if(map.containsKey(request.getFluid())) {
						long addition = ((long) containedAmount) + map.get(request.getFluid());
						containedAmount = addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition;
					}
				}
			}
			if(fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				for(FluidTankInfo tank:tanks) {
					if(tank == null) continue;
					FluidStack liquid;
					if((liquid = tank.fluid) != null) {
						if(request.getFluid() == FluidIdentifier.get(liquid)) {
							long addition = ((long) containedAmount) + liquid.amount;
							containedAmount = addition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) addition;
						}
					}
				}
			}
		}
		FluidLogisticsPromise promise = new FluidLogisticsPromise();
		promise.liquid = request.getFluid();
		promise.amount = Math.min(request.amountLeft(), containedAmount - donePromises);
		promise.sender = this;
		if(promise.amount > 0) {
			request.addPromise(promise);
		}
	}

	@Override
	public void fullFill(FluidLogisticsPromise promise, IRequestFluid destination) {
		manager.add(promise, destination);
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}


	@Override //work in progress, currently not active code.
	public Set<ItemIdentifier> getSpecificInterests() {
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			boolean fallback = true;
			if(SimpleServiceLocator.specialTankHandler.hasHandlerFor(pair.getValue1())) {
				ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(pair.getValue1());
				if(handler instanceof ISpecialTankAccessHandler) {
					fallback = false;
					Map<FluidIdentifier, Long> map = ((ISpecialTankAccessHandler)handler).getAvailableLiquid(pair.getValue1());
					for(FluidIdentifier ident:map.keySet()) {
						l1.add(ident.getItemIdentifier());
					}
				}
			}
			if(fallback) {
				FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
				for(FluidTankInfo tank:tanks) {
					if(tank == null) continue;
					FluidStack liquid;
					if((liquid = tank.fluid) != null && liquid.fluidID != 0) {
						FluidIdentifier ident = FluidIdentifier.get(liquid);
						l1.add(ident.getItemIdentifier());
					}
				}
			}
		}
		return l1;
	}

}
