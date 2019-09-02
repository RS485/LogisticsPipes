package logisticspipes.textures.provider;

import java.util.ArrayList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.renderer.IIconProvider;

public class LPPipeIconProvider implements IIconProvider {

	@SideOnly(Side.CLIENT)
	private ArrayList<TextureAtlasSprite> icons;

	public LPPipeIconProvider() {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			icons = new ArrayList<>();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(int iconIndex) {
		return icons.get(iconIndex);
	}

	@SideOnly(Side.CLIENT)
	public void setIcon(int index, TextureAtlasSprite icon) {
		while (icons.size() < index + 1) {
			icons.add(null);
		}
		icons.set(index, icon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(Object iconRegister) {}
}
