package logisticspipes.proxy.buildcraft.recipeprovider;

import java.util.List;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.silicon.TileAssemblyTable;

public class AssemblyTable implements ICraftingRecipeProvider {

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return (tile instanceof TileAssemblyTable);
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		if (!(tile instanceof TileAssemblyTable)) {
			return false;
		}

		TileAssemblyTable table = (TileAssemblyTable) tile;

		//current pipe inputs/outputs
		final ItemIdentifierInventory inputs = new ItemIdentifierInventory(inventory.getSizeInventory() - 2, "AssemblyTableDummyInv", 64, false);
		for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
			inputs.setInventorySlotContents(i, inventory.getIDStackInSlot(i));
		}
		ItemStack output = inventory.getStackInSlot(inventory.getSizeInventory() - 2);

		//see if there's a recipe planned in the table that matches the current pipe settings, if yes take the next, otherwise take the first
		FlexibleRecipe<ItemStack> firstRecipe = null;
		FlexibleRecipe<ItemStack> nextRecipe = null;
		boolean takeNext = false;
		for (IFlexibleRecipe<ItemStack> r : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			if (!(r instanceof FlexibleRecipe)) {
				continue;
			}
			if (!((FlexibleRecipe<ItemStack>) r).inputFluids.isEmpty()) {
				continue;
			}
			if (table.isPlanned(r)) {
				if (firstRecipe == null) {
					firstRecipe = (FlexibleRecipe<ItemStack>) r;
				}
				if (takeNext) {
					nextRecipe = (FlexibleRecipe<ItemStack>) r;
					break;
				}
				if (output != null && ItemStack.areItemStacksEqual(output, ((FlexibleRecipe<ItemStack>) r).output)) {
					if (((FlexibleRecipe<ItemStack>) r).canBeCrafted(new IFlexibleCrafter() { // Read Proxy to IInventory

								@Override
								public int getCraftingItemStackSize() {
									return inputs.getSizeInventory();
								}

								@Override
								public ItemStack getCraftingItemStack(int paramInt) {
									return inputs.getStackInSlot(paramInt);
								}

								@Override
								public int getCraftingFluidStackSize() {
									return 0;
								}

								@Override
								public FluidStack getCraftingFluidStack(int paramInt) {
									return null;
								}

								@Override
								public ItemStack decrCraftingItemStack(int paramInt1, int paramInt2) {
									return null;
								}

								@Override
								public FluidStack decrCraftingFluidStack(int paramInt1, int paramInt2) {
									return null;
								}
							})) {
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
		inventory.setInventorySlotContents(inventory.getSizeInventory() - 2, nextRecipe.output);
		try {
			for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
				inventory.clearInventorySlotContents(i);
			}
			int i = 0;
			for (Object input : nextRecipe.inputItems) {
				ItemStack processed = null;
				if (input instanceof String) {
					List<ItemStack> ores = OreDictionary.getOres((String) input);
					if (ores != null && ores.size() > 0) {
						input = ores.get(0);
					}
				} else if (input instanceof ItemStack) {
					processed = (ItemStack) input;
				} else if (input instanceof Item) {
					processed = new ItemStack((Item) input);
				} else if (input instanceof Block) {
					processed = new ItemStack((Block) input, 1, 0);
				} else if (input instanceof Integer) {
					processed = null;
				} else {
					throw new IllegalArgumentException("Unknown Object passed to recipe!");
				}
				if (processed != null) {
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
