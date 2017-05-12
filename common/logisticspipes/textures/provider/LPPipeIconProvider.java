package logisticspipes.textures.provider;

import java.util.ArrayList;

import logisticspipes.renderer.IIconProvider;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LPPipeIconProvider implements IIconProvider {

	public ArrayList<TextureAtlasSprite> icons = new ArrayList<>();

	@Override
	public TextureAtlasSprite getIcon(int iconIndex) {
		return icons.get(iconIndex);
	}

	public void setIcon(int index, TextureAtlasSprite icon) {
		while (icons.size() < index + 1) {
			icons.add(null);
		}
		icons.set(index, icon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureMap iconRegister) {}
}
