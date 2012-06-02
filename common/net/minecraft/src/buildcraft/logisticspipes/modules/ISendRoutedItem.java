package net.minecraft.src.buildcraft.logisticspipes.modules;

import java.util.UUID;

import net.minecraft.src.ItemStack;

public interface ISendRoutedItem {
	public UUID getSourceUUID();
	public void sendStack(ItemStack stack);
	public void sendStack(ItemStack stack, UUID destination);
}
