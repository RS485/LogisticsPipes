package logisticspipes.pipes;

import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.pipes.basic.RoutedPipe;

public class PipeItemsApiaristSink extends RoutedPipe {
	
	private ModuleApiaristSink sinkModule;

	public PipeItemsApiaristSink(int itemID) {
		super(new TemporaryLogic(), itemID);
		sinkModule = new ModuleApiaristSink();
		sinkModule.registerHandler(null, null, this, this);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_APIARIST_SINK_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return sinkModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}
