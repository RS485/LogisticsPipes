package logisticspipes.pipes.basic.liquid;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.Pair;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.core.IMachine;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public abstract class LiquidRoutedPipe extends RoutedPipe implements IItemTravelingHook {

	private WorldUtil worldUtil;
	
	public LiquidRoutedPipe(int itemID) {
		super(new PipeLiquidTransportLogistics(), new TemporaryLogic(), itemID);
		((PipeTransportItems) transport).travelHook = this;
		worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		worldUtil = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
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
		WorldUtil util = new WorldUtil(worldObj, xCoord, yCoord, zCoord);
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
		if(!this.isPipeConnected(tile, dir)) return false;
		if(tile instanceof TileGenericPipe) {
			if(!flag) return false;
			if(((TileGenericPipe)tile).pipe == null || !(((TileGenericPipe)tile).pipe.transport instanceof ITankContainer)) return false;
		}
		return true;
	}


	/* IItemTravelingHook */

	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		((PipeTransportLogistics)pipe).markChunkModified(tile);
	}

}
