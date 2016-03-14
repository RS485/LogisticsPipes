package logisticspipes.modules;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleAdvancedExtractorMK2 extends ModuleAdvancedExtractor {

	public ModuleAdvancedExtractorMK2() {
		super();
	}

	@Override
	protected int ticksToAction() {
		return 20;
	}

	@Override
	protected int neededEnergy() {
		return 8;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleAdvancedExtractorMK2");
	}
}
