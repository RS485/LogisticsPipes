/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.items;

import java.util.List;


import logisticspipes.LogisticsPipes;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

public class LogisticsItem extends Item {
	
	public LogisticsItem(int i, IIcon icon) {
		this.itemIcon = icon;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IIconRegister) {
		this.itemIcon = par1IIconRegister.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.",""));
	}

	@Override
	public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{ LogisticsPipes.LPCreativeTab };
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		if(addShiftInfo()) {
			StringUtil.addShiftAddition(par1ItemStack, par3List);
		}
	}
	
	public boolean addShiftInfo() {
		return true;
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.translate(getUnlocalizedName(itemstack));
	}
}
