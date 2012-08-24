package net.minecraft.src.buildcraft.krapht.recipeproviders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;

import net.minecraft.src.krapht.SimpleInventory;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.InventoryCrafting;

public class RollingMachine implements ICraftingRecipeProvider {

	public static Class tileRollingMachineClass;
	public static Method getCraftMatrixMethod;

	public static boolean load() {
		try {
			tileRollingMachineClass = Class.forName("railcraft.common.utility.TileRollingMachine");
			getCraftMatrixMethod = tileRollingMachineClass.getMethod("getCraftMatrix");
		} catch (Exception ex) {
			ModLoader.getLogger().fine("Necessary classes from Railcraft were not found");
			return false;
		}
		return true;
	}

	public boolean canOpenGui(TileEntity tile) {
		return tileRollingMachineClass.isInstance(tile);
	}

	public ItemStack getResult(InventoryCrafting inventorycrafting) {
		if (inventorycrafting == null)
			return null;
		try {
			Class c = Class.forName("railcraft.common.util.crafting.RollingMachineCraftingManager");
			Method inst = c.getMethod("getInstance");
			Object instance = inst.invoke(null);
			Method findMatchingRecipe = c.getMethod("findMatchingRecipe", InventoryCrafting.class);
			return (ItemStack)findMatchingRecipe.invoke(instance, inventorycrafting);
		} catch (Exception ex) {
			ModLoader.getLogger().fine("getResult fail");
		}		
		return null;
	}

	public InventoryCrafting getCraftMatrix(TileEntity tile) {
		try {
			return (InventoryCrafting) getCraftMatrixMethod.invoke(tile);
		} catch (Exception ex) {
			ModLoader.getLogger().fine("getCraftMatrix fail");
		}		
		return null;
	}


	public boolean importRecipe(TileEntity tile, SimpleInventory inventory) {
		if (!tileRollingMachineClass.isInstance(tile))
			return false;

		InventoryCrafting craftMatrix = getCraftMatrix(tile);
		if (craftMatrix == null)
			return false;

		ItemStack result = getResult(craftMatrix);

		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 1) {
				break;
			}
			final ItemStack newStack = craftMatrix.getStackInSlot(i) == null ? null : craftMatrix.getStackInSlot(i).copy();
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
