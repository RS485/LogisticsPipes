package logisticspipes.pipes;

import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.config.Configs;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class PipeLogisticsChassiMk2 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk2(Item item) {
		super(item);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI2_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return Configs.CHASSI_SLOTS_ARRAY[1];
	}

}
