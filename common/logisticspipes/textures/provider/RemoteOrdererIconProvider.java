package logisticspipes.textures.provider;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;
import logisticspipes.LogisticsPipes;

public class RemoteOrdererIconProvider implements IIconProvider {
	public static final int RemoteOrdererDefault 		=  0;
	public static final int RemoteOrdererBlack 		=  1;
	//@TODO put colors here
	private boolean registered = false;
	private static int maxIcons = 17; 
	@SideOnly(Side.CLIENT)
	private Icon[] _icons;
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		if(iconIndex>maxIcons)
			return _icons[0];
		else
			return _icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		if(registered==true)
			return;
		_icons=new Icon[maxIcons];
		for(int i=0;i<maxIcons;i++)
		{
			_icons[i]=iconRegister.registerIcon("logisticpipes:"+LogisticsPipes.LogisticsRemoteOrderer.getUnlocalizedName()+"/"+i);
		}
	}

}
