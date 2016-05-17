package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

import logisticspipes.items.RemoteOrderer;

public class RecipeManager {

	public static final List<IRecipeProvider> recipeProvider = new ArrayList<>();
	public static final LocalCraftingManager craftingManager = new LocalCraftingManager();

	public static void registerRecipeClasses() {
		RecipeSorter
				.register("logisticspipes:shapedore", LPShapedOreRecipe.class, SHAPED, "after:minecraft:shaped before:minecraft:shapeless");
		RecipeSorter
				.register("logisticspipes:shapelessore", LPShapelessOreRecipe.class, SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter
				.register("logisticspipes:shapelessreset", ShapelessResetRecipe.class, SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter
				.register("logisticspipes:shapelessorderer", LocalCraftingManager.ShapelessOrdererRecipe.class, SHAPELESS, "after:minecraft:shapeless");
	}

	public static void loadRecipes() {
		recipeProvider.forEach(IRecipeProvider::loadRecipes);
	}

	@AllArgsConstructor
	@Getter
	public static class RecipeIndex {
		private char index;
		private Object value;
	}

	@AllArgsConstructor
	@Getter
	public static class RecipeLayout {
		private String line1;
		private String line2;
		private String line3;
	}

	@AllArgsConstructor
	@Getter
	public static class RecipeLayoutSmall {
		private String line1;
		private String line2;
	}

	public static class LocalCraftingManager {
		private CraftingManager craftingManager = CraftingManager.getInstance();

		public LocalCraftingManager() {
		}

		@SuppressWarnings("unchecked")
		public void addRecipe(ItemStack stack, CraftingDependency dependent, Object... objects) {
			List<Object> result = new ArrayList<>();
			final boolean[] addRecipe = {true};
			Arrays.stream(objects).forEach(o -> {
				if(!addRecipe[0]) return;
				if (o instanceof RecipeLayout) {
					result.add(((RecipeLayout) o).getLine1());
					result.add(((RecipeLayout) o).getLine2());
					result.add(((RecipeLayout) o).getLine3());
				} else if (o instanceof RecipeLayoutSmall) {
					result.add(((RecipeLayoutSmall) o).getLine1());
					result.add(((RecipeLayoutSmall) o).getLine2());
				} else if (o instanceof RecipeIndex) {
					result.add(((RecipeIndex) o).getIndex());
					result.add(((RecipeIndex) o).getValue());
					if(((RecipeIndex) o).getValue() == null) {
						addRecipe[0] = false;
						return;
					}
				} else {
					result.add(o);
				}
			});
			if(!addRecipe[0]) return;
			craftingManager.getRecipeList().add(new LPShapedOreRecipe(stack, dependent, result.toArray()));
		}

		@SuppressWarnings("unchecked")
		public void addOrdererRecipe(ItemStack stack, String dye, ItemStack orderer) {
			craftingManager.getRecipeList().add(new ShapelessOrdererRecipe(stack, new Object[]{dye, orderer}));
		}

		@SuppressWarnings("unchecked")
		public void addShapelessRecipe(ItemStack stack, CraftingDependency dependent, Object... objects) {
			craftingManager.getRecipeList().add(new LPShapelessOreRecipe(stack, dependent, objects));
		}

		@SuppressWarnings("unchecked")
		public void addShapelessResetRecipe(Item item, int meta) {
			craftingManager.getRecipeList().add(new ShapelessResetRecipe(item, meta));
		}

		public class ShapelessOrdererRecipe extends ShapelessOreRecipe {
			public ShapelessOrdererRecipe(ItemStack result, Object... recipe) {
				super(result, recipe);
			}

			@Override
			public ItemStack getCraftingResult(InventoryCrafting var1) {
				ItemStack result = super.getCraftingResult(var1);
				for (int i = 0; i < var1.getInventoryStackLimit(); i++) {
					ItemStack stack = var1.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof RemoteOrderer) {
						result.setTagCompound(stack.getTagCompound());
						break;
					}
				}
				return result;
			}
		}
	}
}
