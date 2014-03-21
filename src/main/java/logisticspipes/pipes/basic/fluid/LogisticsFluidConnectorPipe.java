package logisticspipes.pipes.basic.fluid;

import logisticspipes.textures.Textures;
import net.minecraft.item.Item;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsFluidConnectorPipe extends Pipe<LogisitcsFluidConnectionTransport> {

	public LogisticsFluidConnectorPipe(Item item) {
		super(new LogisitcsFluidConnectionTransport(),  item);

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
