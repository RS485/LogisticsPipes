package logisticspipes.pipes.basic.liquid;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.LogisticsPipes;
import logisticspipes.textures.Textures;
import logisticspipes.textures.provider.DummyProvider;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.pipes.PipeLogicGold;

public class LogisticsLiquidConnectorPipe extends Pipe {

	public LogisticsLiquidConnectorPipe(int itemID) {
		super(new LogisitcsLiquidConnectionTransport(), new PipeLogicGold(), itemID);

		((PipeTransportLiquids) transport).flowRate = 40;
		((PipeTransportLiquids) transport).travelDelay = 4;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		// TODO Auto-generated method stub
		//return LogisticsPipes.dummyIconProvider;
		return new DummyProvider();
	}


	@Override
	public int getIconIndex(ForgeDirection direction) {
		return Textures.LOGISTICSPIPE_LIQUID_CONNECTOR;
	}
}
