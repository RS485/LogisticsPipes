/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemAttributes;

import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.SpecialWrappedInventoryFactory;

public class InventoryUtilFactory {

	public static final InventoryUtilFactory INSTANCE = new InventoryUtilFactory();

	private InventoryUtilFactory() {}

	private final LinkedList<SpecialWrappedInventoryFactory<?>> handler = new LinkedList<>();

	public void registerHandler(SpecialWrappedInventoryFactory<?> invHandler) {
		handler.addLast(invHandler);
	}

	public SpecialInventoryHandler getUtil(World world, BlockPos pos, Direction dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return handler.stream()
				.map(factory -> factory.getUtil(world, pos, dir, hideOnePerStack, hideOne, cropStart, cropEnd))
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	@Nullable
	public WrappedInventory getInventoryUtil(World world, BlockPos pos, Direction dir) {
		return getHidingInventoryUtil(world, pos, dir, false, false, 0, 0);
	}

	@Nullable
	public WrappedInventory getHidingInventoryUtil(World world, BlockPos pos, Direction dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		WrappedInventory util = getUtil(world, pos, dir, hideOnePerStack, hideOne, cropStart, cropEnd);
		if (util == null) {
			FixedItemInv inv = ItemAttributes.FIXED_INV.get(world, pos, SearchOptions.inDirection(dir));
			if (inv.getSlotCount() > 0) {
				util = new WrappedInventoryImpl(inv.getSubInv(cropStart, inv.getSlotCount() - cropEnd), hideOnePerStack, hideOne);
			}
		}
		return util;
	}
}
