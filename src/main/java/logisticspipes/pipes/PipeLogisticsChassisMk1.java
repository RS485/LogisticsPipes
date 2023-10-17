package logisticspipes.pipes;

import net.minecraft.item.Item;

import logisticspipes.config.Configs;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeLogisticsChassisMk1 extends PipeLogisticsChassis {

	public PipeLogisticsChassisMk1(Item item) {
		super(item);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI1_TEXTURE;
	}

	@Override
	public int getChassisSize() {
		return Configs.CHASSIS_SLOTS_ARRAY[0];
	}
}
