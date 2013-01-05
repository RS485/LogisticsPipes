package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.pipes.basic.liquid.LogisticsLiquidSection;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class PipeLiquidBasic extends LiquidRoutedPipe implements ILiquidSink {
	
	public PipeLiquidBasic(int itemID) {
		super(itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_BASIC;
	}

	@Override
	public void enabledUpdateEntity() {
		WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		int validDirections = 0;
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).sideTanks[dir.ordinal()];
			TileEntity tile = worldUtil.getAdjacentTileEntitie(dir);
			if(!(tile instanceof ITankContainer)) continue;
			if(!this.isPipeConnected(tile)) continue;
			if(tile instanceof TileGenericPipe) {
				if(((TileGenericPipe)tile).pipe == null || !(((TileGenericPipe)tile).pipe.transport instanceof ITankContainer)) continue;
			}
			validDirections++;
			if(tank.getLiquid() == null) continue;
			int filled = ((ITankContainer)tile).fill(dir.getOpposite(), tank.getLiquid(), true);
			if(filled == 0) continue;
			LiquidStack drain = tank.drain(filled, true);
			if(drain == null || filled != drain.amount) {
				if(LogisticsPipes.DEBUG) {
					throw new UnsupportedOperationException("Liquid Multiplication");
				}
			}
		}
		if(validDirections == 0) return;
		LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).internalTank;
		LiquidStack stack = tank.getLiquid();
		if(stack == null) return;
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldUtil.getAdjacentTileEntitie(dir);
			if(!(tile instanceof ITankContainer)) continue;
			if(!this.isPipeConnected(tile)) continue;
			if(tile instanceof TileGenericPipe) {
				if(((TileGenericPipe)tile).pipe == null || !(((TileGenericPipe)tile).pipe.transport instanceof ITankContainer)) continue;
			}
			LogisticsLiquidSection tankSide = ((PipeLiquidTransportLogistics)this.transport).sideTanks[dir.ordinal()];
			stack = tank.getLiquid();
			if(stack == null) continue;
			stack = stack.copy();
			int filled = tankSide.fill(stack , true);
			if(filled == 0) continue;
			LiquidStack drain = tank.drain(filled, true);
			if(drain == null || filled != drain.amount) {
				if(LogisticsPipes.DEBUG) {
					throw new UnsupportedOperationException("Liquid Multiplication");
				}
			}
		}
	}

	@Override
	public int sinkAmount(LiquidStack stack) {
		LiquidIdentifier ident = LiquidIdentifier.get(stack);
		//TODO check filter
		
		WorldUtil worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
		int amount = 0;
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldUtil.getAdjacentTileEntitie(dir);
			if(!(tile instanceof ITankContainer)) continue;
			if(!this.isPipeConnected(tile)) continue;
			if(tile instanceof TileGenericPipe) {
				if(((TileGenericPipe)tile).pipe == null || !(((TileGenericPipe)tile).pipe.transport instanceof ITankContainer)) continue;
			}
			LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).sideTanks[dir.ordinal()];
			amount += tank.fill(stack, false);
			if(amount == stack.amount) {
				return amount;
			}
		}
		if(ident.getName().equals("water")) {
			return amount;
		}
		return 0;
	}
	
	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if(!(tile instanceof ITankContainer)) return;
		if(data.output.ordinal() >= ForgeDirection.VALID_DIRECTIONS.length) return;
		if(data.item == null || data.item.getItemStack() == null || !(data.item.getItemStack().getItem() instanceof LogisticsLiquidContainer)) return;
		((PipeTransportItems)this.transport).scheduleRemoval(data.item);
		LiquidStack liquid = SimpleServiceLocator.logisticsLiquidManager.getLiquidFromContainer(data.item.getItemStack());
		if(liquid != null) {
			int filled = ((PipeLiquidTransportLogistics)this.transport).sideTanks[data.output.ordinal()].fill(liquid, true);
			if(filled != liquid.amount) {
				liquid.amount -= filled;
				filled = ((PipeLiquidTransportLogistics)this.transport).internalTank.fill(liquid, true);
				if(filled != liquid.amount) {
					if(data.item instanceof IRoutedItem) {
						liquid.amount -= filled;
						IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(liquid), worldObj);
						routedItem.setDestination(((IRoutedItem)data.item).getSource());
						routedItem.setTransportMode(TransportMode.Passive);
						this.queueRoutedItem(routedItem, data.output.getOpposite());
					}
				}
			}
		}
	}

	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {}
}
