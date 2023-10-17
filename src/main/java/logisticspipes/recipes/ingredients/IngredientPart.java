package logisticspipes.recipes.ingredients;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import com.google.gson.JsonObject;

import logisticspipes.recipes.CraftingPartRecipes;
import logisticspipes.recipes.CraftingParts;

@SuppressWarnings("unused") // used in recipes
public class IngredientPart implements IIngredientFactory {

	public enum PartType {
		chip_basic,
		chip_advanced,
		chip_fpga,
		;

		@Nonnull
		public ItemStack getStack(CraftingParts parts) {
			switch (this) {
				case chip_basic:
					return parts.getChipBasic();
				case chip_advanced:
					return parts.getChipAdvanced();
				case chip_fpga:
					return parts.getChipFpga();
			}
			throw new IllegalStateException();
		}
	}

	@Nonnull
	@Override
	public Ingredient parse(JsonContext context, JsonObject json) {
		PartType ct = PartType.valueOf(json.get("part").getAsString());
		return new IngredientPart.Impl(ct);
	}

	private static class Impl extends Ingredient {

		Impl(PartType type) {
			super(CraftingPartRecipes.getCraftingPartList().stream().map(type::getStack).toArray(ItemStack[]::new));
		}

	}

}
