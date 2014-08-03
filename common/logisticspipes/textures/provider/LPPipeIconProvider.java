package logisticspipes.textures.provider;

import java.util.ArrayList;

import logisticspipes.renderer.IIconProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LPPipeIconProvider implements IIconProvider {
	public ArrayList<IIcon> icons = new ArrayList<IIcon>();
	
	@Override
	public IIcon getIcon(int iconIndex) {
		return icons.get(iconIndex);
	}
	
	public void setIcon(int index, IIcon icon) {
		while(icons.size() < index + 1) {
			icons.add(null);
		}
		icons.set(index, icon);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {}
}
