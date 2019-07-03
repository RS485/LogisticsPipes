package logisticspipes.pipes;

import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.config.Configs;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

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
		return Configs.CHASSI_SLOTS_ARRAY[2];
	}

}
