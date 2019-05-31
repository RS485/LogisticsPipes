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

import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import appeng.api.storage.IMEInventoryHandler;

import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class InventoryUtilFactory {

	private final LinkedList<SpecialInventoryHandler> handler = new LinkedList<>();

	public void registerHandler(SpecialInventoryHandler invHandler) {
		if (invHandler.init()) {
			handler.addLast(invHandler);
			LogisticsPipes.log.info("Loaded SpecialInventoryHandler: " + invHandler.getClass().getCanonicalName());
		} else {
			LogisticsPipes.log.warn("Could not load SpecialInventoryHandler: " + invHandler.getClass().getCanonicalName());
		}
	}

	public SpecialInventoryHandler getUtilForInv(ICapabilityProvider inv, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		if (!(inv instanceof TileEntity)) {
			return null;
		}
		for (SpecialInventoryHandler invHandler : handler) {
			if (invHandler.isType((TileEntity) inv, dir)) {
				return invHandler.getUtilForTile((TileEntity) inv, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
			}
		}
		return null;
	}

	public IInventoryUtil getInventoryUtil(WorldCoordinatesWrapper.AdjacentTileEntity adj) {
		return getHidingInventoryUtil(adj.tileEntity, adj.direction.getOpposite(), false, false, 0, 0);
	}

	public IInventoryUtil getInventoryUtil(TileEntity inv, EnumFacing dir)
	{
		/*int defaultend = 0;
		if(inv != null && inv.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir))
		{
			IItemHandler itemhandler = inv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
			defaultend = itemhandler.getSlots();
		}*/
		return getHidingInventoryUtil(inv, dir, false, false, 0, 0);
	}

	public IInventoryUtil getHidingInventoryUtil(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		IInventoryUtil util = getUtilForInv(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util == null && tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
			util = new InventoryUtil(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir), hideOnePerStack, hideOne, cropStart, cropEnd);
		}
		return util;
	}
}
