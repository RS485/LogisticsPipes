package logisticspipes.pipes;

import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class PipeItemsApiaristSink extends CoreRoutedPipe {

	private ModuleApiaristSink sinkModule;

	public PipeItemsApiaristSink(Item item) {
		super(item);
		sinkModule = new ModuleApiaristSink();
		sinkModule.registerHandler(this, this);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_APIARIST_SINK_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return sinkModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		sinkModule.registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

}
