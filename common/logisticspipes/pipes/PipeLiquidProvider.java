package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;

import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.LiquidRequest;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.routing.LogisticsLiquidOrderManager;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.TileGenericPipe;

public class PipeLiquidProvider extends LiquidRoutedPipe implements ILiquidProvider {
	
	LogisticsLiquidOrderManager manager = new LogisticsLiquidOrderManager();
	
	public PipeLiquidProvider(int itemID) {
		super(itemID);
	}

	@Override
	public void enabledUpdateEntity() {
		if (!manager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;
		
		Pair3<LiquidIdentifier, Integer, IRequestLiquid> order = manager.getFirst();
		int amountToSend = Math.min(order.getValue2(), 5000);
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for(AdjacentTile aTile:wUtil.getAdjacentTileEntities(true)) {
			if(aTile.tile instanceof TileGenericPipe) continue;
			if(!(aTile.tile instanceof ITankContainer)) continue;
			if(!this.isPipeConnected(aTile.tile)) continue;
			if(amountToSend <= 0) break;
			ILiquidTank[] tanks = ((ITankContainer)aTile.tile).getTanks(aTile.orientation.getOpposite());
			for(ILiquidTank tank:tanks) {
				LiquidStack liquid;
				if((liquid = tank.getLiquid()) != null) {
					if(order.getValue1() == LiquidIdentifier.get(liquid)) {
						int amount = Math.min(liquid.amount, amountToSend);
						amountToSend -= amount;
						LiquidStack drained = ((ITankContainer)aTile.tile).drain(aTile.orientation, amount, false);
						if(order.getValue1() == LiquidIdentifier.get(drained)) {
							drained = ((ITankContainer)aTile.tile).drain(aTile.orientation, amount, true);
							ItemStack stack = SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(drained);
							IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, worldObj);
							item.setDestination(order.getValue3().getRouter().getId());
							item.setTransportMode(TransportMode.Active);
							item.setSource(this.getRouter().getId());
							this.queueRoutedItem(item, aTile.orientation);
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
	public Map<LiquidIdentifier, Integer> getAvailableLiquids() {
		Map<LiquidIdentifier, Integer> map = new HashMap<LiquidIdentifier, Integer>();
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for(AdjacentTile aTile:wUtil.getAdjacentTileEntities(true)) {
			if(aTile.tile instanceof TileGenericPipe) continue;
			if(!(aTile.tile instanceof ITankContainer)) continue;
			if(!this.isPipeConnected(aTile.tile)) continue;
			ILiquidTank[] tanks = ((ITankContainer)aTile.tile).getTanks(aTile.orientation.getOpposite());
			for(ILiquidTank tank:tanks) {
				LiquidStack liquid;
				if((liquid = tank.getLiquid()) != null && liquid.itemID != 0) {
					LiquidIdentifier ident = LiquidIdentifier.get(liquid);
					if(map.containsKey(ident)) {
						map.put(ident, map.get(ident) + tank.getLiquid().amount);
					} else {						
						map.put(ident, tank.getLiquid().amount);
					}
				}
			}
		}
		//Reduce Ordered
		for(Pair3<LiquidIdentifier, Integer, IRequestLiquid> pair: manager.getAll()) {
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
	public boolean disconnectPipe(TileEntity tile) {
		return tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe != null && ((TileGenericPipe)tile).pipe.transport instanceof PipeTransportLiquids;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_PROVIDER;
	}

	@Override
	public void canProvide(LiquidRequest request) {
		if(request.isAllDone()) return;
		WorldUtil wUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		for(AdjacentTile aTile:wUtil.getAdjacentTileEntities(true)) {
			if(aTile.tile instanceof TileGenericPipe) continue;
			if(!(aTile.tile instanceof ITankContainer)) continue;
			if(!this.isPipeConnected(aTile.tile)) continue;
			ILiquidTank[] tanks = ((ITankContainer)aTile.tile).getTanks(aTile.orientation.getOpposite());
			for(ILiquidTank tank:tanks) {
				LiquidStack liquid;
				if((liquid = tank.getLiquid()) != null) {
					if(request.getLiquid() == LiquidIdentifier.get(liquid)) {
						LiquidLogisticsPromise promise = new LiquidLogisticsPromise();
						promise.item = request.getLiquid();
						promise.amount = Math.min(request.amountLeft(), liquid.amount);
						promise.sender = this;
						request.addPromise(promise);
					}
				}
			}
		}
	}

	@Override
	public void fullFill(LiquidLogisticsPromise promise, IRequestLiquid destination) {
		manager.add(promise, destination);
	}
}
