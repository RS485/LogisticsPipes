package logisticspipes.modules;

import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleAdvancedExtractorMK3 extends ModuleAdvancedExtractorMK2 {

	public ModuleAdvancedExtractorMK3() {
		super();
	}

	@Override
	protected int ticksToAction() {
		return 1;
	}

	@Override
	protected int itemsToExtract() {
		return 64;
	}

	@Override
	protected int neededEnergy() {
		return 11;
	}

	@Override
	protected ItemSendMode itemSendMode() {
		return ItemSendMode.Fast;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleAdvancedExtractorMK3");
	}
}
