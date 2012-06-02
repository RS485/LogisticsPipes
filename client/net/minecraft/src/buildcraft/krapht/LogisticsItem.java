/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.forge.ITextureProvider;

public class LogisticsItem extends Item implements ITextureProvider{

	public LogisticsItem(int i) {
		super(i);
	}
	
	@Override
	public String getTextureFile() {
		return core_LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, List list) {
		
		//Add special tooltip in tribute to DireWolf
		if (itemstack != null && itemstack.itemID == mod_LogisticsPipes.LogisticsRemoteOrderer.shiftedIndex){
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
				list.add("a.k.a \"Requesting Tool\" - DW20");
			}
		}

		super.addInformation(itemstack, list);
	}
	
	

}
