/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import net.minecraft.inventory.IInventory;
import buildcraft.core.utils.Utils;

public class InventoryUtilFactory {
	public InventoryUtil getInventoryUtil(IInventory inv) {
		return new InventoryUtil(Utils.getInventory(inv), false);
	}
	public InventoryUtil getOneHiddenInventoryUtil(IInventory inv) {
		return new InventoryUtil(Utils.getInventory(inv), true);
	}

}
