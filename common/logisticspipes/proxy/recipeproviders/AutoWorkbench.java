package logisticspipes.proxy.recipeproviders;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.factory.TileAutoWorkbench;

public class AutoWorkbench implements ICraftingRecipeProvider {
	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAutoWorkbench);
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
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

		inventory.compact_first_9();
		
		return true;
	}
}
