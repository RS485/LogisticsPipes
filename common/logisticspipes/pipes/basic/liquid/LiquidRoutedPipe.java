package logisticspipes.pipes.basic.liquid;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.Pair;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.core.IMachine;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public abstract class LiquidRoutedPipe extends CoreRoutedPipe implements IItemTravelingHook {

	private WorldUtil worldUtil;
	
	public LiquidRoutedPipe(int itemID) {
		super(new PipeLiquidTransportLogistics(), new TemporaryLogic(), itemID);
		((PipeTransportItems) transport).travelHook = this;
		worldUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
	}
	
	public LiquidRoutedPipe(BaseRoutingLogic logic, int itemID) {
		super(new PipeLiquidTransportLogistics(), logic, itemID);
		((PipeTransportItems) transport).travelHook = this;
		worldUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
	}
	
	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		worldUtil = new WorldUtil(worldObj, getX(), getY(), getZ());
	}

	@Override
	public boolean logisitcsIsPipeConnected(TileEntity tile) {
		if (tile instanceof ITankContainer) {
			ITankContainer liq = (ITankContainer) tile;

			if (liq.getTanks(ForgeDirection.UNKNOWN) != null && liq.getTanks(ForgeDirection.UNKNOWN).length > 0)
				return true;
		}

		return tile instanceof TileGenericPipe || (tile instanceof IMachine && ((IMachine) tile).manageLiquids());
	}
	
	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if(isLiquidSidedTexture(connection)) {
			return Textures.LOGISTICSPIPE_LIQUID_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}
	
	private boolean isLiquidSidedTexture(ForgeDirection connection) {
		WorldUtil util = new WorldUtil(worldObj, getX(), getY(), getZ());
		TileEntity tile = util.getAdjacentTileEntitie(connection);
		if (tile instanceof ITankContainer) {
			ITankContainer liq = (ITankContainer) tile;

			if (liq.getTanks(ForgeDirection.UNKNOWN) != null && liq.getTanks(ForgeDirection.UNKNOWN).length > 0)
				return true;
		}
		if(tile instanceof TileGenericPipe) {
			return ((TileGenericPipe)tile).pipe instanceof LogisticsLiquidConnectorPipe;
		}
		return false;
	}
	
	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}
	
	/***
	 * @param flag  Weather to list a Nearby Pipe or not
	 */
	
	public final List<Pair<TileEntity,ForgeDirection>> getAdjacentTanks(boolean flag) {
		List<Pair<TileEntity,ForgeDirection>> tileList =  new ArrayList<Pair<TileEntity,ForgeDirection>>();
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = worldUtil.getAdjacentTileEntitie(dir);
			if(!isConnectableTank(tile, dir, flag)) continue;
			tileList.add(new Pair<TileEntity,ForgeDirection>(tile, dir));
		}
		return tileList;
	}
	
	/***
	 * @param tile The connected TileEntity
	 * @param dir  The direction the TileEntity is in relative to the currect pipe
	 * @param flag Weather to list a Nearby Pipe or not
	 */
	
	public final boolean isConnectableTank(TileEntity tile, ForgeDirection dir, boolean flag) {
		if(!(tile instanceof ITankContainer)) return false;
		if(!this.canPipeConnect(tile, dir)) return false;
		if(tile instanceof TileGenericPipe) {
			if(!flag) return false;
			if(((TileGenericPipe)tile).pipe == null || !(((TileGenericPipe)tile).pipe.transport instanceof ITankContainer)) return false;
		}
		return true;
	}
	
	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if(canInsertFromSideToTanks()) {
			int validDirections = 0;
			List<Pair<TileEntity,ForgeDirection>> list = getAdjacentTanks(true);
			for(Pair<TileEntity,ForgeDirection> pair:list) {
				if(pair.getValue1() instanceof TileGenericPipe) {
					if(((TileGenericPipe)pair.getValue1()).pipe instanceof CoreRoutedPipe) continue;
				}
				LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).sideTanks[pair.getValue2().ordinal()];
				validDirections++;
				if(tank.getLiquid() == null) continue;
				int filled = ((ITankContainer)pair.getValue1()).fill(pair.getValue2().getOpposite(), tank.getLiquid(), true);
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
			for(Pair<TileEntity,ForgeDirection> pair:list) {
				if(pair.getValue1() instanceof TileGenericPipe) {
					if(((TileGenericPipe)pair.getValue1()).pipe instanceof CoreRoutedPipe) continue;
				}
				LogisticsLiquidSection tankSide = ((PipeLiquidTransportLogistics)this.transport).sideTanks[pair.getValue2().ordinal()];
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
	}

	public abstract boolean canInsertFromSideToTanks();
	
	public abstract boolean canInsertToTanks();
	
	/* IItemTravelingHook */

	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		((PipeTransportLogistics)pipe).markChunkModified(tile);
		if(canInsertToTanks() && MainProxy.isServer(worldObj)) {
			if(!this.isConnectableTank(tile, data.output, false)) return;
			if(!(data.item instanceof IRoutedItem) || data.item.getItemStack() == null || !(data.item.getItemStack().getItem() instanceof LogisticsLiquidContainer)) return;
			if(this.getRouter().getSimpleID() != ((IRoutedItem)data.item).getDestination()) return;
			((PipeTransportItems)this.transport).scheduleRemoval(data.item);
			LiquidStack liquid = SimpleServiceLocator.logisticsLiquidManager.getLiquidFromContainer(data.item.getItemStack());
			List<Pair<TileEntity,ForgeDirection>> adjTanks = getAdjacentTanks(false);
			//Try to put liquid into all adjacent tanks.
			for (int i = 0; i < adjTanks.size(); i++) {
				Pair<TileEntity,ForgeDirection> pair = adjTanks.get(i);
				ITankContainer tank = (ITankContainer) pair.getValue1();
				ForgeDirection dir = pair.getValue2();
				int filled = tank.fill(dir.getOpposite(), liquid, true);
				liquid.amount -= filled;
				if (liquid.amount != 0) continue;
				return;
			}
			//Try inserting the liquid into the pipe side tank
			int filled = ((PipeLiquidTransportLogistics)this.transport).sideTanks[data.output.ordinal()].fill(liquid, true);
			if(filled == liquid.amount) return;
			liquid.amount -= filled;
			//Try inserting the liquid into the pipe internal tank
			filled = ((PipeLiquidTransportLogistics)this.transport).internalTank.fill(liquid, true);
			if(filled == liquid.amount) return;
			//If liquids still exist,
			liquid.amount -= filled;

			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(liquid), worldObj);
			Pair<Integer, Integer> replies = SimpleServiceLocator.logisticsLiquidManager.getBestReply(liquid, this.getRouter(), routedItem.getJamList());
			int dest = replies.getValue1();
			routedItem.setDestination(dest);
			routedItem.setTransportMode(TransportMode.Passive);
			this.queueRoutedItem(routedItem, data.output.getOpposite());
		}
	}

	@Override
	public boolean isLiquidPipe() {
		return true;
	}
}
