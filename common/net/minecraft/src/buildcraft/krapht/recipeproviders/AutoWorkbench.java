package net.minecraft.src.buildcraft.krapht.recipeproviders;

import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

import net.minecraft.src.krapht.SimpleInventory;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;

public class AutoWorkbench implements ICraftingRecipeProvider {
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAutoWorkbench);
	}


	public boolean importRecipe(TileEntity tile, SimpleInventory inventory) {
		if (!(tile instanceof TileAutoWorkbench))
			return false;

		TileAutoWorkbench bench = (TileAutoWorkbench) tile;
		ItemStack result = bench.findRecipe();

		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < bench.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 1) {
				break;
			}
			final ItemStack newStack = bench.getStackInSlot(i) == null ? null : bench.getStackInSlot(i).copy();
			inventory.setInventorySlotContents(i, newStack);
		}

		// Compact
		for (int i = 0; i < inventory.getSizeInventory() - 1; i++) {
			final ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = ItemIdentifier.get(stackInSlot);
			for (int j = i + 1; j < inventory.getSizeInventory() - 1; j++) {
				final ItemStack stackInOtherSlot = inventory.getStackInSlot(j);
				if (stackInOtherSlot == null) {
					continue;
				}
				if (itemInSlot == ItemIdentifier.get(stackInOtherSlot)) {
					stackInSlot.stackSize += stackInOtherSlot.stackSize;
					inventory.setInventorySlotContents(j, null);
				}
			}
		}

		for (int i = 0; i < inventory.getSizeInventory() - 1; i++) {
			if (inventory.getStackInSlot(i) != null) {
				continue;
			}
			for (int j = i + 1; j < inventory.getSizeInventory() - 1; j++) {
				if (inventory.getStackInSlot(j) == null) {
					continue;
				}
				inventory.setInventorySlotContents(i, inventory.getStackInSlot(j));
				inventory.setInventorySlotContents(j, null);
				break;
			}
		}
		return true;
	}
}
