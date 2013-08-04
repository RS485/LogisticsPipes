package logisticspipes.proxy.recipeproviders;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.factory.TileAutoWorkbench;

public class AutoWorkbench implements ICraftingRecipeProvider {
	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAutoWorkbench);
	}

	@Override
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory) {
		if (!(tile instanceof TileAutoWorkbench))
			return false;

		TileAutoWorkbench bench = (TileAutoWorkbench) tile;
		ItemStack result = bench.findRecipeOutput();
		//ItemStack result = bench.getStackInSlot(TileAutoWorkbench.SLOT_RESULT);
		
		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < bench.craftMatrix.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			final ItemStack newStack = bench.craftMatrix.getStackInSlot(i) == null ? null : bench.craftMatrix.getStackInSlot(i).copy();
			if(newStack!=null && newStack.stackSize>1) // just incase size == 0 somehow.
			newStack.stackSize=1;
			inventory.setInventorySlotContents(i, newStack);
		}

		// Compact
		
		for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
			final ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = ItemIdentifier.get(stackInSlot);
			for (int j = i + 1; j < inventory.getSizeInventory() - 2; j++) {
				final ItemStack stackInOtherSlot = inventory.getStackInSlot(j);
				if (stackInOtherSlot == null) {
					continue;
				}
				if (itemInSlot == ItemIdentifier.get(stackInOtherSlot)) {
					stackInSlot.stackSize += stackInOtherSlot.stackSize;
					inventory.setInventorySlotContents(i, stackInSlot);
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
