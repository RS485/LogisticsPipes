package logisticspipes.proxy.recipeproviders;

import java.util.BitSet;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.request.resources.DictResource;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;

public class LogisticsCraftingTable implements IFuzzyRecipeProvider {

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof LogisticsCraftingTableTileEntity);
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		if (!(tile instanceof LogisticsCraftingTableTileEntity)) {
			return false;
		}

		LogisticsCraftingTableTileEntity bench = (LogisticsCraftingTableTileEntity) tile;
		ItemIdentifierStack result = bench.resultInv.getIDStackInSlot(0);

		if (result == null) {
			return false;
		}

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < bench.matrix.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			ItemStack stackInSlot = bench.matrix.getStackInSlot(i);
			if (!stackInSlot.isEmpty() && stackInSlot.getCount() > 1) {
				stackInSlot = stackInSlot.copy();
				stackInSlot.setCount(1);
			}
			inventory.setInventorySlotContents(i, stackInSlot);
		}

		if (!bench.isFuzzy()) {
			inventory.compactFirst(9);
		}

		return true;
	}

	@Override
	public void importFuzzyFlags(TileEntity tile, ItemIdentifierInventory inventory, DictResource[] flags, DictResource output) {
		if (!(tile instanceof LogisticsCraftingTableTileEntity)) {
			return;
		}

		LogisticsCraftingTableTileEntity bench = (LogisticsCraftingTableTileEntity) tile;

		if (!bench.isFuzzy()) {
			return;
		}

		for (int i = 0; i < bench.fuzzyFlags.length; i++) {
			if (i >= flags.length) {
				break;
			}
			flags[i].loadFromBitSet(bench.fuzzyFlags[i].getBitSet());
		}
		output.loadFromBitSet(bench.outputFuzzyFlags.getBitSet());

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
				if (itemInSlot.equals(stackInOtherSlot.getItem()) && flags[i].getBitSet().equals(flags[j].getBitSet())) {
					stackInSlot.setStackSize(stackInSlot.getStackSize() + stackInOtherSlot.getStackSize());
					inventory.clearInventorySlotContents(j);
					flags[j].loadFromBitSet(new BitSet()); // clear
				}
			}
			inventory.setInventorySlotContents(i, stackInSlot);
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
				flags[i].loadFromBitSet(flags[j].getBitSet());
				inventory.clearInventorySlotContents(j);
				flags[j].loadFromBitSet(new BitSet()); // clear
				break;
			}
		}

		return;
	}
}
