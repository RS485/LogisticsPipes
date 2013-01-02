package logisticspipes.pipes.basic.liquid;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import buildcraft.core.IMachine;
import buildcraft.transport.TileGenericPipe;

public abstract class LiquidRoutedPipe extends RoutedPipe {
	
	public LiquidRoutedPipe(int itemID) {
		super(new PipeLiquidTransportLogistics(), new TemporaryLogic(), itemID);
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
}
