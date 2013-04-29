package logisticspipes.pipes.basic.liquid;

import logisticspipes.textures.Textures;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.pipes.PipeLogicGold;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsLiquidConnectorPipe extends Pipe {

	public LogisticsLiquidConnectorPipe(int itemID) {
		super(new LogisitcsLiquidConnectionTransport(), new PipeLogicGold(), itemID);

		((PipeTransportLiquids) transport).flowRate = 40;
		((PipeTransportLiquids) transport).travelDelay = 4;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return Textures.LPpipeIconProvider;	
	}


	@Override
	public int getIconIndex(ForgeDirection direction) {
		return Textures.LOGISTICSPIPE_LIQUID_CONNECTOR;
	}
}
