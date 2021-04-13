package logisticspipes.proxy.buildcraft.recipeprovider;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.tile.TileAssemblyTable;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;

public class AssemblyTable implements ICraftingRecipeProvider {

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAssemblyTable);
	}

	@Override
	public boolean importRecipe(TileEntity tile, IItemIdentifierInventory inventory) {
		if (!(tile instanceof TileAssemblyTable)) {
			return false;
		}

		TileAssemblyTable table = (TileAssemblyTable) tile;

		//current pipe inputs/outputs
		final ItemIdentifierInventory inputs = new ItemIdentifierInventory(inventory.getSizeInventory() - 2, "AssemblyTableDummyInv", 64, false);
		for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
			inputs.setInventorySlotContents(i, inventory.getIDStackInSlot(i));
		}
		final ItemStack output = inventory.getStackInSlot(inventory.getSizeInventory() - 2);

		//see if there's a recipe planned in the table that matches the current pipe settings, if yes take the next, otherwise take the first
		AssemblyRecipeBasic firstRecipe = null;
		AssemblyRecipeBasic nextRecipe = null;
		boolean takeNext = false;
		for (AssemblyRecipe r : AssemblyRecipeRegistry.REGISTRY.values()) {
			if (!(r instanceof AssemblyRecipeBasic)) {
				continue;
			}
			if (table.recipesStates.entrySet().stream().filter(it -> it.getKey().recipe == r)
					.anyMatch(it -> it.getValue() != EnumAssemblyRecipeState.POSSIBLE)) {
				if (firstRecipe == null) {
					firstRecipe = (AssemblyRecipeBasic) r;
				}
				if (takeNext) {
					nextRecipe = (AssemblyRecipeBasic) r;
					break;
				}
				if (!output.isEmpty() && r.getOutputPreviews().stream().anyMatch(it -> ItemStack.areItemStacksEqual(output, it))) {
					if (!r.getOutputs(inputs.toNonNullList()).isEmpty()) {
						takeNext = true;
					}
				}
			}
		}
		if (nextRecipe == null) {
			nextRecipe = firstRecipe;
		}
		if (nextRecipe == null) {
			return false;
		}

		// Import
		inventory.setInventorySlotContents(inventory.getSizeInventory() - 2, nextRecipe.getOutputPreviews().stream().findFirst().orElse(ItemStack.EMPTY));
		try {
			for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
				inventory.clearInventorySlotContents(i);
			}
			int i = 0;
			for (Object input : nextRecipe.getInputsFor(inventory.getStackInSlot(inventory.getSizeInventory() - 2))) {
				ItemStack processed = ItemStack.EMPTY;
				if (input instanceof String) {
					NonNullList<ItemStack> ores = OreDictionary.getOres((String) input);
					if (ores != null && ores.size() > 0) {
						processed = ores.get(0);
					}
				} else if (input instanceof ItemStack) {
					processed = (ItemStack) input;
				} else if (input instanceof Item) {
					processed = new ItemStack((Item) input);
				} else if (input instanceof Block) {
					processed = new ItemStack((Block) input, 1, 0);
				} else if (input instanceof Integer) {
					// was null
				} else {
					throw new IllegalArgumentException("Unknown Object passed to recipe!");
				}
				if (!processed.isEmpty()) {
					inventory.setInventorySlotContents(i, processed);
					++i;
				}
			}
		} catch (ClassCastException e) {
			// TODO: make it show a nice error or
			// remove this hack altogether.
		}

		inventory.getSlotAccess().compactFirst(9);

		return true;
	}
}