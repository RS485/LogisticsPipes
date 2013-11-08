package logisticspipes.proxy.recipeproviders;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class LogisticsCraftingTable implements ICraftingRecipeProvider {
	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof LogisticsCraftingTableTileEntity);
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		if (!(tile instanceof LogisticsCraftingTableTileEntity))
			return false;

		LogisticsCraftingTableTileEntity bench = (LogisticsCraftingTableTileEntity) tile;
		ItemIdentifierStack result = bench.resultInv.getIDStackInSlot(0);
		
		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < bench.matrix.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			final ItemStack newStack = bench.matrix.getStackInSlot(i) == null ? null : bench.matrix.getStackInSlot(i).copy();
			if(newStack!=null && newStack.stackSize>1) // just incase size == 0 somehow.
			newStack.stackSize=1;
			inventory.setInventorySlotContents(i, newStack);
		}
		
		inventory.compact_first_9();
		
		return true;
	}
}
