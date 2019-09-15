/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;
import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;

public class InventoryUtilFactory {

	public static final InventoryUtilFactory INSTANCE = new InventoryUtilFactory();

	private InventoryUtilFactory() {}

	private final LinkedList<SpecialInventoryHandler> handler = new LinkedList<>();

	public void registerHandler(SpecialInventoryHandler invHandler) {
		if (invHandler.init()) {
			handler.addLast(invHandler);
			LogisticsPipes.log.info("Loaded SpecialInventoryHandler: " + invHandler.getClass().getCanonicalName());
		} else {
			LogisticsPipes.log.warn("Could not load SpecialInventoryHandler: " + invHandler.getClass().getCanonicalName());
		}
	}

	public SpecialInventoryHandler getUtilForInv(ICapabilityProvider inv, Direction dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		if (!(inv instanceof BlockEntity)) {
			return null;
		}
		for (SpecialInventoryHandler invHandler : handler) {
			if (invHandler.isType((BlockEntity) inv, dir)) {
				return invHandler.getUtilForTile((BlockEntity) inv, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
			}
		}
		return null;
	}

	@Nullable
	public WrappedInventory getInventoryUtil(NeighborBlockEntity<BlockEntity> adj) {
		return getHidingInventoryUtil(adj.getBlockEntity(), adj.getOurDirection(), false, false, 0, 0);
	}

	@Nullable
	public WrappedInventory getInventoryUtil(BlockEntity inv, Direction dir) {
		return getHidingInventoryUtil(inv, dir, false, false, 0, 0);
	}

	@Nullable
	public WrappedInventory getHidingInventoryUtil(BlockEntity tile, Direction dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		WrappedInventory util = getUtilForInv(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util == null && tile != null && tile.hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, dir)) {
			util = new WrappedInventoryImpl(tile.getCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, dir), hideOnePerStack, hideOne, cropStart, cropEnd);
		}
		return util;
	}
}
