package logisticspipes.buildcraft.krapht.pipes;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.RoutedPipe;
import logisticspipes.buildcraft.krapht.logic.TemporaryLogic;
import logisticspipes.buildcraft.logisticspipes.modules.ILogisticsModule;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleApiaristSink;

public class PipeItemsApiaristSink extends RoutedPipe {
	
	private ModuleApiaristSink sinkModule;

	public PipeItemsApiaristSink(int itemID) {
		super(new TemporaryLogic(), itemID);
		sinkModule = new ModuleApiaristSink();
		sinkModule.registerHandler(null, null, this);
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_APIARIST_SINK_TEXTURE;
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
