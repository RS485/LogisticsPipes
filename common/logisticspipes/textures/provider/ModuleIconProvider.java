package logisticspipes.textures.provider;

import logisticspipes.LogisticsPipes;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class ModuleIconProvider implements IIconProvider {
	
	private boolean registered = false;
	private static int maxIcons = 22; 
	//used to translate damage values (temporary from original texture sheet)
	private static int[] rowMap= {13,2,2,2,3};
	@SideOnly(Side.CLIENT)
	private Icon[] _icons;
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		//@TODO fix texture mappings
		return _icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		if(registered==true)
			return;
		_icons=new Icon[maxIcons];
		for(int i=0;i<maxIcons;i++)
		{
			_icons[i]=iconRegister.registerIcon("logisticpipes:"+LogisticsPipes.ModuleItem.getUnlocalizedName()+"/"+i);
		}
	}

}
