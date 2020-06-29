/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import network.rs485.logisticspipes.connection.NeighborTileEntity;

public class InventoryUtilFactory {

	private final ArrayList<SpecialInventoryHandler.Factory> handlerFactories = new ArrayList<>();

	public void registerHandler(@Nonnull SpecialInventoryHandler.Factory handlerFactory) {
		if (handlerFactory.init()) {
			handlerFactories.add(handlerFactory);
			LogisticsPipes.log.info("Loaded SpecialInventoryHandler.Factory: " + handlerFactory.getClass().getCanonicalName());
		} else {
			LogisticsPipes.log.warn("Could not load SpecialInventoryHandler.Factory: " + handlerFactory.getClass().getCanonicalName());
		}
	}

	@Nullable
	public SpecialInventoryHandler getUtilForInv(@Nonnull TileEntity inv, @Nullable EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return handlerFactories.stream()
				.filter(factory -> factory.isType(inv, dir))
				.map(factory -> factory.getUtilForTile(inv, dir, hideOnePerStack, hideOne, cropStart, cropEnd))
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	@Nullable
	public IInventoryUtil getInventoryUtil(@Nonnull NeighborTileEntity<TileEntity> adj) {
		return getHidingInventoryUtil(adj.getTileEntity(), adj.getOurDirection(), false, false, 0, 0);
	}

	@Nullable
	public IInventoryUtil getInventoryUtil(TileEntity inv, EnumFacing dir) {
		return getHidingInventoryUtil(inv, dir, false, false, 0, 0);
	}

	@Nullable
	public IInventoryUtil getHidingInventoryUtil(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		if (tile != null) {
			IInventoryUtil util = getUtilForInv(tile, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
			if (util != null) {
				return util;
			} else if (tile.hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, dir)) {
				return new InventoryUtil(tile.getCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY, dir), hideOnePerStack, hideOne, cropStart, cropEnd);
			}
		}
		return null;
	}
}
