package logisticspipes.textures.provider;

import java.util.ArrayList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;

public class LPPipeIconTransformerProvider {

	@SideOnly(Side.CLIENT)
	private ArrayList<TextureTransformation> icons;

	public LPPipeIconTransformerProvider() {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			icons = new ArrayList<>();
		}
	}

	@SideOnly(Side.CLIENT)
	public TextureTransformation getIcon(int iconIndex) {
		return icons.get(iconIndex);
	}

	@SideOnly(Side.CLIENT)
	public void setIcon(int index, TextureAtlasSprite icon) {
		while (icons.size() < index + 1) {
			icons.add(null);
		}
		if (icons.get(index) != null) {
			icons.get(index).update(icon);
		} else {
			icons.set(index, SimpleServiceLocator.cclProxy.createIconTransformer(icon));
		}
	}
}
