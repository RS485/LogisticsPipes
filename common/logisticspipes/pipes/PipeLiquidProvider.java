package logisticspipes.pipes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.LiquidRequest;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.routing.LogisticsLiquidOrderManager;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;
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
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			if(amountToSend <= 0) break;
			ILiquidTank[] tanks = ((ITankContainer)pair.getValue1()).getTanks(pair.getValue2().getOpposite());
			for(ILiquidTank tank:tanks) {
				LiquidStack liquid;
				if((liquid = tank.getLiquid()) != null) {
					if(order.getValue1() == LiquidIdentifier.get(liquid)) {
						int amount = Math.min(liquid.amount, amountToSend);
						amountToSend -= amount;
						LiquidStack drained = ((ITankContainer)pair.getValue1()).drain(pair.getValue2(), amount, false);
						if(order.getValue1() == LiquidIdentifier.get(drained)) {
							drained = ((ITankContainer)pair.getValue1()).drain(pair.getValue2(), amount, true);
							ItemStack stack = SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(drained);
							IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, worldObj);
							item.setDestination(order.getValue3().getRouter().getId());
							item.setTransportMode(TransportMode.Active);
							item.setSource(this.getRouter().getId());
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
	public Map<LiquidIdentifier, Integer> getAvailableLiquids() {
		Map<LiquidIdentifier, Integer> map = new HashMap<LiquidIdentifier, Integer>();
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			ILiquidTank[] tanks = ((ITankContainer)pair.getValue1()).getTanks(pair.getValue2().getOpposite());
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
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			ILiquidTank[] tanks = ((ITankContainer)pair.getValue1()).getTanks(pair.getValue2().getOpposite());
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
	
	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if(!this.isConnectableTank(tile, data.output, false)) return;
		if(!(data.item instanceof IRoutedItem) || data.item.getItemStack() == null || !(data.item.getItemStack().getItem() instanceof LogisticsLiquidContainer)) return;
		if(!this.getRouter().getId().equals(((IRoutedItem)data.item).getDestination())) return;
		((PipeTransportItems)this.transport).scheduleRemoval(data.item);
		LiquidStack liquid = SimpleServiceLocator.logisticsLiquidManager.getLiquidFromContainer(data.item.getItemStack());
		int netAmount = liquid.amount;
		int totalFilled = 0;
		List<Pair<TileEntity,ForgeDirection>> adjTanks = getAdjacentTanks(false);
		//Try to put liquid into all adjacent tanks.
		for (int i = 0; i < adjTanks.size(); i++) {
			Pair<TileEntity,ForgeDirection> pair = adjTanks.get(i);
			ITankContainer tank = (ITankContainer) pair.getValue1();
			ForgeDirection dir = (ForgeDirection) pair.getValue2();
			int filled = tank.fill(dir, liquid, true);
			totalFilled = totalFilled + filled;
			liquid.amount -= filled;
			if (liquid.amount != filled) continue;
			return;
		}
		//If liquids still exist,
		if (netAmount > totalFilled) {
			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(liquid), worldObj);
			Pair<UUID, Integer> replies = SimpleServiceLocator.logisticsLiquidManager.getBestReply(liquid, this.getRouter(), routedItem.getJamList());
			UUID dest = (UUID) replies.getValue1();
			routedItem.setDestination(dest);
			routedItem.setTransportMode(TransportMode.Passive);
			this.queueRoutedItem(routedItem, data.output.getOpposite());
		}
	}
}
