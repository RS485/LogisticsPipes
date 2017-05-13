package logisticspipes.proxy.object3d.interfaces;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface TextureTransformation extends I3DOperation {

	void update(TextureAtlasSprite registerIcon);

	TextureAtlasSprite getTexture();
}
