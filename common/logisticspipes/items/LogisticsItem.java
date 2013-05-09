/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsItem extends Item {

	public LogisticsItem(int i) {
		super(i);
	}
	
	public LogisticsItem(int i,Icon icon) {
		super(i);
		this.itemIcon = icon;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
		if(this.itemIcon == null)
			this.itemIcon = par1IconRegister.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.",""));
	}

	@Override
	public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{ LogisticsPipes.LPCreativeTab };
	}
}
