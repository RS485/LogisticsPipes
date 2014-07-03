package logisticspipes.proxy.recipeproviders;

import java.util.List;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.AssemblyRecipeManager.AssemblyRecipe;
import buildcraft.silicon.TileAssemblyTable;

public class AssemblyTable implements ICraftingRecipeProvider {
	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAssemblyTable);
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		if (!(tile instanceof TileAssemblyTable))
			return false;

		TileAssemblyTable table = (TileAssemblyTable) tile;

		//current pipe inputs/outputs
		ItemIdentifierInventory inputs = new ItemIdentifierInventory(inventory.getSizeInventory() - 2, "AssemblyTableDummyInv", 64, false);
		for(int i = 0; i< inventory.getSizeInventory() - 2; i++)
			inputs.setInventorySlotContents(i, inventory.getIDStackInSlot(i));
		ItemStack output = inventory.getStackInSlot(inventory.getSizeInventory() - 2);

		//see if there's a recipe planned in the table that matches the current pipe settings, if yes take the next, otherwise take the first
		AssemblyRecipe firstRecipe = null;
		AssemblyRecipe nextRecipe = null;
		boolean takeNext = false;
		for (AssemblyRecipe r : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			if(table.isPlanned(r)) {
				if(firstRecipe == null) {
					firstRecipe = r;
				}
				if(takeNext) {
					nextRecipe = r;
					break;
				}
				if(output != null && ItemStack.areItemStacksEqual(output, r.output)) {
					if(r.canBeDone(inputs)) {
						takeNext = true;
					}
				}
			}
		}
		if(nextRecipe == null)
			nextRecipe = firstRecipe;
		if(nextRecipe == null)
			return false;

		// Import
		inventory.setInventorySlotContents(inventory.getSizeInventory() - 2, nextRecipe.output);
		try {
			for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
				inventory.clearInventorySlotContents(i);
			}
			int i = 0;
			for(Object input : nextRecipe.getInputs()) {
				ItemStack processed = null;
				if (input instanceof String) {
					List<ItemStack> ores = OreDictionary.getOres((String) input);
					if(ores != null && ores.size() > 0)
						input = ores.get(0);
				} else if (input instanceof ItemStack) {
					processed = (ItemStack)input;
				} else if (input instanceof Item) {
					processed = new ItemStack((Item) input);
				} else if (input instanceof Block) {
					processed = new ItemStack((Block) input, 1, 0);
				} else if (input instanceof Integer) {
					processed = null;
				} else {
					throw new IllegalArgumentException("Unknown Object passed to recipe!");
				}
				if(processed != null) {
					inventory.setInventorySlotContents(i, processed);
					++i;
				}
			}
		} catch (ClassCastException e) {// TODO: make it show a nice error or
										// remove this hack altogether.

		}
		// Compact
		inventory.compact_first(9);
		
		return true;
	}
}
