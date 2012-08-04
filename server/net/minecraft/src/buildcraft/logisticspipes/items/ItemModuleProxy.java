package net.minecraft.src.buildcraft.logisticspipes.items;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.krapht.LogisticsItem;

public abstract class ItemModuleProxy extends LogisticsItem {

	public ItemModuleProxy(int i) {
		super(i);
	}

	public abstract String getModuleDisplayName(ItemStack itemstack);
	
}
