package logisticspipes.textures.provider;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class LPPipeIconProvider implements IIconProvider {
	public Icon icons[];
	
	//@SideOnly(Side.CLIENT)
	public LPPipeIconProvider(int limit) {
		icons=new Icon[limit];
	}
	@Override
	public Icon getIcon(int iconIndex) {
		return icons[iconIndex%icons.length];
	}
	public void setIcon(int index, Icon icon)
	{
		icons[index]=icon;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		

	}

}
