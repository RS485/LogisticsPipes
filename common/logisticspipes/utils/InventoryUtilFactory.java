/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import network.rs485.logisticspipes.connection.NeighborTileEntity;

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

	@Nullable
	public IInventoryUtil getInventoryUtil(NeighborTileEntity<TileEntity> adj) {
		return getHidingInventoryUtil(adj.getTileEntity(), adj.getOurDirection(), false, false, 0, 0);
	}

	@Nullable
	public IInventoryUtil getInventoryUtil(TileEntity inv, EnumFacing dir) {
		return getHidingInventoryUtil(inv, dir, false, false, 0, 0);
	}

	@Nullable
	public IInventoryUtil getHidingInventoryUtil(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		IInventoryUtil util = getUtilForInv(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util == null && tile != null && tile.hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, dir)) {
			util = new InventoryUtil(tile.getCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, dir), hideOnePerStack, hideOne, cropStart, cropEnd);
		}
		return util;
	}
}
