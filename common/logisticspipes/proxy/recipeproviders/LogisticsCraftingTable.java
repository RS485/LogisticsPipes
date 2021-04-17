package logisticspipes.proxy.recipeproviders;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.inventory.FuzzySlotAccess;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;
import network.rs485.logisticspipes.inventory.SlotAccess;
import network.rs485.logisticspipes.property.BitSetProperty;

public class LogisticsCraftingTable implements IFuzzyRecipeProvider {

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof LogisticsCraftingTableTileEntity);
	}

	@Override
	public boolean importRecipe(TileEntity tile, IItemIdentifierInventory inventory) {
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
			inventory.getSlotAccess().compactFirst(9);
		}

		return true;
	}

	@Override
	public void importFuzzyFlags(TileEntity tile, SlotAccess slotAccess, BitSetProperty fuzzyFlags) {
		if (!(tile instanceof LogisticsCraftingTableTileEntity)) {
			return;
		}

		LogisticsCraftingTableTileEntity bench = (LogisticsCraftingTableTileEntity) tile;

		if (!bench.isFuzzy()) {
			return;
		}

		fuzzyFlags.replaceWith(bench.fuzzyFlags);
		new FuzzySlotAccess(slotAccess, fuzzyFlags).compactFirst(9);
	}

}
