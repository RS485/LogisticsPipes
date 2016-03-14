package logisticspipes.modules;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

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
	public TextureAtlasSprite getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleExtractorMk2");
	}
}
