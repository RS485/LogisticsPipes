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
	public String getModuleModelPath() {
		return "itemModule/ModuleExtractorMk2";
	}
}
