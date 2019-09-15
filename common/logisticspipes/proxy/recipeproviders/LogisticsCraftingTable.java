package logisticspipes.proxy.recipeproviders;

import java.util.BitSet;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.proxy.interfaces.FuzzyRecipeProvider;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemStack;
import network.rs485.logisticspipes.routing.request.Resource;

public class LogisticsCraftingTable implements FuzzyRecipeProvider {

	@Override
	public boolean canOpenGui(BlockEntity tile) {
		return (tile instanceof LogisticsCraftingTableTileEntity);
	}

	@Override
	public boolean importRecipe(BlockEntity tile, Inventory inventory) {
		if (!(tile instanceof LogisticsCraftingTableTileEntity)) {
			return false;
		}

		LogisticsCraftingTableTileEntity bench = (LogisticsCraftingTableTileEntity) tile;
		ItemStack result = bench.resultInv.getInvStack(0);

		if (result == null) {
			return false;
		}

		inventory.setInvStack(9, result);

		// Import
		for (int i = 0; i < bench.matrix.getInvSize(); i++) {
			if (i >= inventory.getInvSize() - 2) {
				break;
			}
			final ItemStack newStack = bench.matrix.getInvStack(i).copy();
			if (!newStack.isEmpty() && newStack.getCount() > 1) {
				newStack.setCount(1);
			}
			inventory.setInvStack(i, newStack);
		}

		if (!bench.isFuzzy()) {
			inventory.compactFirst(9);
		}

		return true;
	}

	@Override
	public void importFuzzyFlags(BlockEntity tile, Inventory inventory, Resource.Dict[] flags, Resource.Dict output) {
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
			final ItemStack stackInSlot = inventory.getIDStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = stackInSlot.getItem();
			for (int j = i + 1; j < 9; j++) {
				final ItemStack stackInOtherSlot = inventory.getIDStackInSlot(j);
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
