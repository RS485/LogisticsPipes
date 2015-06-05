package logisticspipes.renderer;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IIconProvider {

	@SideOnly(Side.CLIENT)
	IIcon getIcon(int iconIndex);

	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);

}
