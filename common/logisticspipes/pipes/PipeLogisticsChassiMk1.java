package logisticspipes.pipes;

import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class PipeLogisticsChassiMk1 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk1(Item item) {
		super(item);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI1_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 1;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/chassipipe_size1.png");

	@Override
	public ResourceLocation getChassiGUITexture() {
		return PipeLogisticsChassiMk1.TEXTURE;
	}

}
