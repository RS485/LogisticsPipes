package logisticspipes.modules;

import logisticspipes.pipes.PipeItemsCraftingLogistics;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleCrafterMK2 extends ModuleCrafter {

	public ModuleCrafterMK2() {}

	public ModuleCrafterMK2(PipeItemsCraftingLogistics parentPipe) {
		super(parentPipe);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCrafterMK2");
	}

	@Override
	protected int neededEnergy() {
		return 15;
	}

	@Override
	protected int itemsToExtract() {
		return 64;
	}

	@Override
	protected int stacksToExtract() {
		return 1;
	}

}
