package logisticspipes.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import net.minecraftforge.oredict.OreDictionary;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;

public class NBTIngredient extends Ingredient {

	private final ItemStack[] matchingStacks;
	private IntList matchingStacksPacked;

	protected NBTIngredient(ItemStack... stacks) {
		this.matchingStacks = stacks;
	}

	@Nonnull
	public ItemStack[] getMatchingStacks() {
		return this.matchingStacks;
	}

	@Override
	public boolean apply(@Nullable final ItemStack inputStack) {
		if (inputStack == null) return false;
		for (final ItemStack stack : matchingStacks) {
			if (stack.getItem() == inputStack.getItem()) {
				final int metadata = stack.getMetadata();
				if ((metadata == OreDictionary.WILDCARD_VALUE || metadata == inputStack.getMetadata()) && ItemStack.areItemStackTagsEqual(inputStack, stack)) {
					return true;
				}
			}
		}
		return false;
	}

	@Nonnull
	@Override
	public IntList getValidItemStacksPacked() {
		if (matchingStacksPacked == null) {
			matchingStacksPacked = new IntArrayList(this.matchingStacks.length);
			for (final ItemStack stack : this.matchingStacks) {
				matchingStacksPacked.add(RecipeItemHelper.pack(stack));
			}
			matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
		}
		return matchingStacksPacked;
	}

	@Override
	protected void invalidate() {
		matchingStacksPacked = null;
	}

	@Nonnull
	public static Ingredient fromStacks(final ItemStack... stacks) {
		if (stacks.length > 0) {
			for (final ItemStack itemstack : stacks) {
				if (!itemstack.isEmpty()) {
					return new NBTIngredient(stacks);
				}
			}
		}

		return EMPTY;
	}

}
