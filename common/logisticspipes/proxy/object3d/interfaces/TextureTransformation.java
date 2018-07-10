package logisticspipes.proxy.object3d.interfaces;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface TextureTransformation extends I3DOperation {

	@SideOnly(Side.CLIENT)
	void update(TextureAtlasSprite registerIcon);

	@SideOnly(Side.CLIENT)
	TextureAtlasSprite getTexture();
}
