/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface ItemRequester extends Comparable<ItemRequester>, Requester {

	void itemCouldNotBeSend(ItemStack item, IAdditionalTargetInformation info);

	@Override
	int compareTo(@Nonnull ItemRequester other);
}
