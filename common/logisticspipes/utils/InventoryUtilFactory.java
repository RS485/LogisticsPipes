/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class InventoryUtilFactory {

	private final LinkedList<SpecialInventoryHandler> handler = new LinkedList<SpecialInventoryHandler>();

	public void registerHandler(SpecialInventoryHandler invHandler) {
		if (invHandler.init()) {
			handler.addLast(invHandler);
			LogisticsPipes.log.info("Loaded SpecialInventoryHandler: " + invHandler.getClass().getCanonicalName());
		} else {
			LogisticsPipes.log.warn("Could not load SpecialInventoryHandler: " + invHandler.getClass().getCanonicalName());
		}
	}

	private TileEntity getTileEntityFromInventory(IInventory inv) {
		if (inv instanceof TileEntity) {
			return (TileEntity) inv;
		} else if (inv instanceof SidedInventoryMinecraftAdapter) {
			if (((SidedInventoryMinecraftAdapter) inv)._sidedInventory instanceof TileEntity) {
				return (TileEntity) ((SidedInventoryMinecraftAdapter) inv)._sidedInventory;
			}
		}
		return null;
	}

	public SpecialInventoryHandler getUtilForInv(IInventory inv, ForgeDirection dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		TileEntity tile = getTileEntityFromInventory(inv);
		if (tile == null) {
			return null;
		}
		for (SpecialInventoryHandler invHandler : handler) {
			if (invHandler.isType(tile)) {
				return invHandler.getUtilForTile(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
			}
		}
		return null;
	}

	@Deprecated
	public IInventoryUtil getInventoryUtil(IInventory inv) {
		return getInventoryUtil(inv, ForgeDirection.UNKNOWN);
	}

	public IInventoryUtil getInventoryUtil(IInventory inv, ForgeDirection dir) {
		return getHidingInventoryUtil(inv, dir, false, false, 0, 0);
	}

	public IInventoryUtil getHidingInventoryUtil(IInventory inv, ForgeDirection dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		IInventoryUtil util = getUtilForInv(inv, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util == null) {
			util = new InventoryUtil(InventoryHelper.getInventory(inv), hideOnePerStack, hideOne, cropStart, cropEnd);;
		}
		return util;
	}

	public IInventoryUtil getFuzzyInventoryUtil(IInventory inv) {
		return new FuzzyInventoryUtil(inv);
	}
}
