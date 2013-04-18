package logisticspipes.textures.provider;

import logisticspipes.LogisticsPipes;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class DummyProvider implements IIconProvider {

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		if(iconIndex==1)
			return LogisticsPipes.teststuff2;
		return LogisticsPipes.teststuff;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		// TODO Auto-generated method stub

	}

}
