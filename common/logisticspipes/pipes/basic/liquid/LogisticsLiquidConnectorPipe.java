package logisticspipes.pipes.basic.liquid;

import logisticspipes.textures.Textures;
import net.minecraftforge.common.ForgeDirection;
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
	public Icon getTextureIcon()  {
		return Textures.BASE_TEXTURE_FILE;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return Textures.LOGISTICSPIPE_LIQUID_CONNECTOR;
	}
}
