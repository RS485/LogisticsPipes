/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

public class InventoryUtilFactory {
	private final List<SpecialInventoryHandler> handler = new ArrayList<SpecialInventoryHandler>();

	public void registerHandler(SpecialInventoryHandler invHandler) {
		if(invHandler.init()) {
			handler.add(invHandler);
		}
	}

	private TileEntity getTileEntityFromInventory(IInventory inv) {
		if(inv instanceof TileEntity) {
			return (TileEntity) inv;
		} else if(inv instanceof SidedInventoryAdapter) {
			if(((SidedInventoryAdapter) inv)._sidedInventory instanceof TileEntity) {
				return (TileEntity) ((SidedInventoryAdapter) inv)._sidedInventory;
			}
		}
		return null;
	}

	private IInventoryUtil getUtilForInv(IInventory inv, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		TileEntity tile = getTileEntityFromInventory(inv);
		if(tile == null) return null;
		for(SpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return invHandler.getUtilForTile(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
			}
		}
		return null;
	}

	public IInventoryUtil getInventoryUtil(IInventory inv) {
		return getHidingInventoryUtil(inv, false, false, 0, 0);
	}

	public IInventoryUtil getHidingInventoryUtil(IInventory inv, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		IInventoryUtil util = getUtilForInv(inv, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util == null) {
			util = new InventoryUtil(InventoryHelper.getInventory(inv), hideOnePerStack, hideOne, cropStart, cropEnd);;
		}
		return util;
	}
}
