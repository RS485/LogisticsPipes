package logisticspipes.renderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IIconProvider {

	@SideOnly(Side.CLIENT)
	TextureAtlasSprite getIcon(int iconIndex);

	void registerIcons(Object textureMap);
}
