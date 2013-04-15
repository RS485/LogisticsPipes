package logisticspipes.textures.provider;

import logisticspipes.textures.Textures;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class DummyProvider implements IIconProvider {

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		return Textures.BASE_TEXTURE_FILE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		// TODO Auto-generated method stub

	}

}
