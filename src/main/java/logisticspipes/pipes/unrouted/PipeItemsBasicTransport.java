package logisticspipes.pipes.unrouted;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.textures.Textures;
import logisticspipes.transport.PipeTransportLogistics;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PipeItemsBasicTransport extends CoreUnroutedPipe {

	public PipeItemsBasicTransport(Item item) {
		super(new PipeTransportLogistics(false), item);
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public int getIconIndex(EnumFacing direction) {
		return Textures.LOGISTICSPIPE_BASIC_TRANSPORT_TEXTURE.normal;
	}

	@Override
	public int getTextureIndex() {
		return Textures.LOGISTICSPIPE_BASIC_TRANSPORT_TEXTURE.newTexture;
	}

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return LogisticsRenderPipe.secondRenderer;
	}

}
