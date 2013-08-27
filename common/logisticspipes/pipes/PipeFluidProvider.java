package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;

public class PipeFluidProvider extends FluidRoutedPipe implements IFluidProvider {
	
	LogisticsFluidOrderManager manager = new LogisticsFluidOrderManager();
	
	public PipeFluidProvider(int itemID) {
		super(itemID);
	}

	@Override
	public void enabledUpdateEntity() {
		if (!manager.hasOrders() || getWorld().getWorldTime() % 6 != 0) return;
		
		Pair3<FluidIdentifier, Integer, IRequestFluid> order = manager.getFirst();
		int amountToSend = Math.min(order.getValue2(), 5000);
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			if(amountToSend <= 0) break;
			FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
			for(FluidTankInfo tank:tanks) {
				FluidStack liquid;
				if((liquid = tank.fluid) != null) {
					if(order.getValue1() == FluidIdentifier.get(liquid)) {
						int amount = Math.min(liquid.amount, amountToSend);
						amountToSend -= amount;
						FluidStack drained = ((IFluidHandler)pair.getValue1()).drain(pair.getValue2(), amount, false);
						if(drained != null && order.getValue1() == FluidIdentifier.get(drained)) {
							drained = ((IFluidHandler)pair.getValue1()).drain(pair.getValue2(), amount, true);
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
		if(amountToSend > 0) {
			manager.sendFailed();
		}
	}

	@Override
	public Map<FluidIdentifier, Integer> getAvailableFluids() {
		Map<FluidIdentifier, Integer> map = new HashMap<FluidIdentifier, Integer>();
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
			for(FluidTankInfo tank:tanks) {
				FluidStack liquid;
				if((liquid = tank.fluid) != null && liquid.fluidID != 0) {
					FluidIdentifier ident = FluidIdentifier.get(liquid);
					if(map.containsKey(ident)) {
						map.put(ident, map.get(ident) + tank.fluid.amount);
					} else {						
						map.put(ident, tank.fluid.amount);
					}
				}
			}
		}
		//Reduce Ordered
		for(Pair3<FluidIdentifier, Integer, IRequestFluid> pair: manager.getAll()) {
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
			FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
			for(FluidTankInfo tank:tanks) {
				FluidStack liquid;
				if((liquid = tank.fluid) != null) {
					if(request.getFluid() == FluidIdentifier.get(liquid)) {
						containedAmount += liquid.amount;
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
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();;
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			FluidTankInfo[] tanks = ((IFluidHandler)pair.getValue1()).getTankInfo(pair.getValue2().getOpposite());
			for(FluidTankInfo tank:tanks) {
				FluidStack liquid;
				if((liquid = tank.fluid) != null && liquid.fluidID != 0) {
					FluidIdentifier ident = FluidIdentifier.get(liquid);
					l1.add(ident.getItemIdentifier());
				}
			}
		}
		return l1;
	}

}
