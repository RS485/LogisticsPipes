package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.IInventory;
import net.minecraft.src.buildcraft.api.Orientations;

public interface IInventoryProvider {
	public IInventory getInventory();
	public IInventory getRawInventory();
	public Orientations inventoryOrientation();
}
