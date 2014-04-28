package logisticspipes.pipes.basic.fluid;

import java.nio.channels.Pipe;

import logisticspipes.textures.Textures;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsFluidConnectorPipe extends Pipe<LogisitcsFluidConnectionTransport> {

	public LogisticsFluidConnectorPipe(int itemID) {
		super(new LogisitcsFluidConnectionTransport(),  itemID);

		((PipeTransportFluids) transport).flowRate = 40;
		((PipeTransportFluids) transport).travelDelay = 4;
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
