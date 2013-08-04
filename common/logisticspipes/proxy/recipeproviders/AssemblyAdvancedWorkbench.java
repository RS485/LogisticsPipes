package logisticspipes.proxy.recipeproviders;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.silicon.TileAdvancedCraftingTable;

public class AssemblyAdvancedWorkbench implements ICraftingRecipeProvider {
	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAdvancedCraftingTable);
	}

	@Override
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory) {
		if (!(tile instanceof TileAdvancedCraftingTable))
			return false;

		TileAdvancedCraftingTable bench = (TileAdvancedCraftingTable) tile;
		ItemStack result = bench.getOutputSlot().getStackInSlot(0);

		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < bench.getCraftingSlots().getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			final ItemStack newStack = bench.getCraftingSlots().getStackInSlot(i) == null ? null : bench.getCraftingSlots().getStackInSlot(i).copy();
			inventory.setInventorySlotContents(i, newStack);
		}

		// Compact
		for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
			final ItemIdentifierStack stackInSlot = inventory.getIDStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = stackInSlot.getItem();
			for (int j = i + 1; j < inventory.getSizeInventory() - 2; j++) {
				final ItemIdentifierStack stackInOtherSlot = inventory.getIDStackInSlot(j);
				if (stackInOtherSlot == null) {
					continue;
				}
				if (itemInSlot == stackInOtherSlot.getItem()) {
					stackInSlot.stackSize += stackInOtherSlot.stackSize;
					inventory.setInventorySlotContents(i,stackInSlot);
					inventory.clearInventorySlotContents(j);
				}
			}
		}

		for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
			if (inventory.getStackInSlot(i) != null) {
				continue;
			}
			for (int j = i + 1; j < inventory.getSizeInventory() - 2; j++) {
				if (inventory.getStackInSlot(j) == null) {
					continue;
				}
				inventory.setInventorySlotContents(i, inventory.getIDStackInSlot(j));
				inventory.clearInventorySlotContents(j);
				break;
			}
		}
		return true;
	}
}
