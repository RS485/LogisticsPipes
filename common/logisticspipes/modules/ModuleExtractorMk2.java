package logisticspipes.modules;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleExtractorMk2 extends ModuleExtractor {

	public ModuleExtractorMk2() {
		super();
	}

	@Override
	protected int ticksToAction() {
		return 20;
	}

	@Override
	protected int neededEnergy() {
		return 7;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIconTexture(TextureMap register) {
		return register.registerSprite(new ResourceLocation("logisticspipes:itemModule/ModuleExtractorMk2"));
	}
}
