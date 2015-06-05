package logisticspipes.pipes;

import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

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
		return 3;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/chassipipe_size3.png");

	@Override
	public ResourceLocation getChassiGUITexture() {
		return PipeLogisticsChassiMk3.TEXTURE;
	}

}
