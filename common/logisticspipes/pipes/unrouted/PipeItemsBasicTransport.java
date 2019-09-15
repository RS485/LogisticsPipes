package logisticspipes.pipes.unrouted;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.transport.PipeTransportLogistics;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PipeItemsBasicTransport extends CoreUnroutedPipe {

	public PipeItemsBasicTransport() {
		super(new PipeTransportLogistics(false), Items.AIR);
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return LogisticsRenderPipe.secondRenderer;
	}
}
