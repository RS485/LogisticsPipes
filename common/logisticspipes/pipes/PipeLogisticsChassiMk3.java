package logisticspipes.pipes;

import net.minecraft.item.Item;

import logisticspipes.config.Configs;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeLogisticsChassiMk3 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk3(Item item) {
		super(item);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI3_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return Configs.CHASSIS_SLOTS_ARRAY[2];
	}

}
