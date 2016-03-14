package logisticspipes.textures.provider;

import java.util.ArrayList;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class LPPipeIconTransformerProvider {

	public ArrayList<TextureTransformation> icons = new ArrayList<>();

	public TextureTransformation getIcon(int iconIndex) {
		return icons.get(iconIndex);
	}

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
