package logisticspipes.proxy.recipeproviders;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class LogisticsCraftingTable implements IFuzzyRecipeProvider {
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
		
		if(!bench.isFuzzy())
			inventory.compact_first(9);
		
		return true;
	}

	@Override
	public boolean importFuzzyFlags(TileEntity tile, ItemIdentifierInventory inventory, int[] flags)
	{
		if (!(tile instanceof LogisticsCraftingTableTileEntity))
			return false;

		LogisticsCraftingTableTileEntity bench = (LogisticsCraftingTableTileEntity) tile;
		
		if(!bench.isFuzzy())
			return false;
		
		for (int i = 0; i < bench.fuzzyFlags.length; i++) {
			if(i >= flags.length) {
				break;
			}
			flags[i] = (bench.fuzzyFlags[i].use_od ? 1 : 0) |
					   (bench.fuzzyFlags[i].ignore_dmg ? 2 : 0) |
					   (bench.fuzzyFlags[i].ignore_nbt ? 4 : 0) |
					   (bench.fuzzyFlags[i].use_category ? 8 : 0);
		}
		
		//compact with fuzzy flags
		
		for (int i = 0; i < 9; i++) {
			final ItemIdentifierStack stackInSlot = inventory.getIDStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = stackInSlot.getItem();
			for (int j = i + 1; j < 9; j++) {
				final ItemIdentifierStack stackInOtherSlot = inventory.getIDStackInSlot(j);
				if (stackInOtherSlot == null) {
					continue;
				}
				if (itemInSlot.equals(stackInOtherSlot.getItem()) && flags[i] == flags[j]) {
					stackInSlot.setStackSize(stackInSlot.getStackSize() + stackInOtherSlot.getStackSize());
					inventory.clearInventorySlotContents(j);
					flags[j] = 0;
				}
			}
			inventory.setInventorySlotContents(i,stackInSlot);
		}
		
		for (int i = 0; i < 9; i++) {
			if (inventory.getStackInSlot(i) != null) {
				continue;
			}
			for (int j = i + 1; j < 9; j++) {
				if (inventory.getStackInSlot(j) == null) {
					continue;
				}
				inventory.setInventorySlotContents(i, inventory.getStackInSlot(j));
				flags[i] = flags[j];
				inventory.clearInventorySlotContents(j);
				flags[j] = 0;
				break;
			}
		}
		
		return true;
	}
}
