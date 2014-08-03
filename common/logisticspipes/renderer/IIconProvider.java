package logisticspipes.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public interface IIconProvider {

	@SideOnly(Side.CLIENT)
	IIcon getIcon(int iconIndex);

	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);
	
}
