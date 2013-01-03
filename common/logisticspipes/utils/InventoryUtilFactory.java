/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.inventory.IInventory;

public class InventoryUtilFactory {

	public IInventoryUtil getInventoryUtil(IInventory inv) {
		return getHidingInventoryUtil(inv, false, false, 0, 0);
	}

	public IInventoryUtil getHidingInventoryUtil(IInventory inv, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		IInventoryUtil util = SimpleServiceLocator.specialinventory.getUtilForInv(inv, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util != null) {
			return util;
		}
		return new InventoryUtil(InventoryHelper.getInventory(inv), hideOnePerStack, hideOne, cropStart, cropEnd);
	}
}
