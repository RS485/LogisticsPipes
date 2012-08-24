/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht;

import java.util.List;

import logisticspipes.mod_LogisticsPipes;


import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class LogisticsItem extends Item {

	public LogisticsItem(int i) {
		super(i);
	}
	
	@Override
	public String getTextureFile() {
		return mod_LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE;
	}
}
