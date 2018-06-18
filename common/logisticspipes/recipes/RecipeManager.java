package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.GameData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

import logisticspipes.LPConstants;
import logisticspipes.items.RemoteOrderer;

public class RecipeManager {

	public static final List<IRecipeProvider> recipeProvider = new ArrayList<>();
	public static final LocalCraftingManager craftingManager = new LocalCraftingManager();

	public static void registerRecipeClasses() {
		/*RecipeSorter
				.register("logisticspipes:shapedore", LPShapedOreRecipe.class, SHAPED, "after:minecraft:shaped before:minecraft:shapeless");
		RecipeSorter
				.register("logisticspipes:shapelessore", LPShapelessOreRecipe.class, SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter
				.register("logisticspipes:shapelessreset", ShapelessResetRecipe.class, SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter
				.register("logisticspipes:shapelessorderer", LocalCraftingManager.ShapelessOrdererRecipe.class, SHAPELESS, "after:minecraft:shapeless");
				*/
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
	@AllArgsConstructor
	@Getter
	public static class RecipeLayoutSmaller {
		private String line1;
	}

	public static class LocalCraftingManager {
		public LocalCraftingManager() {
		}

		@SuppressWarnings("unchecked")
		public void addRecipe(ItemStack stack, Object... objects) {
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
				} else if (o instanceof RecipeLayoutSmaller) {
					result.add(((RecipeLayoutSmaller) o).getLine1());
				} else if (o instanceof RecipeIndex) {
					result.add(((RecipeIndex) o).getIndex());
					result.add(((RecipeIndex) o).getValue());
					if(((RecipeIndex) o).getValue() == null) {
						addRecipe[0] = false;
					}
				} else {
					result.add(o);
				}
			});
			if(!addRecipe[0]) return;

			GameData.register_impl(new ShapedOreRecipe(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), stack, result.toArray()).setRegistryName(getFreeRecipeResourceLocation(stack)));
		}

		@SuppressWarnings("unchecked")
		public void addOrdererRecipe(ItemStack stack, String dye, ItemStack orderer) {
			GameData.register_impl(new ShapelessOrdererRecipe(getFreeRecipeResourceLocation(stack), stack, dye, orderer));
		}

		@SuppressWarnings("unchecked")
		public void addShapelessRecipe(ItemStack stack, Object... objects) {
			GameData.register_impl(new ShapelessOreRecipe(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), stack, objects).setRegistryName(getFreeRecipeResourceLocation(stack)));
		}

		@SuppressWarnings("unchecked")
		public void addShapelessResetRecipe(Item item, int meta) {
			ShapelessResetRecipe value = new ShapelessResetRecipe(item, meta);
			value.setRegistryName(getFreeRecipeResourceLocation(item));
			GameData.register_impl(value);
		}

		public class ShapelessOrdererRecipe extends ShapelessOreRecipe {
			public ShapelessOrdererRecipe(ResourceLocation registryName, ItemStack result, Object... recipe) {
				super(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), result, recipe);
				setRegistryName(registryName);
			}

			@Override
			public ItemStack getCraftingResult(InventoryCrafting var1) {
				ItemStack result = super.getCraftingResult(var1);
				for (int i = 0; i < var1.getInventoryStackLimit(); i++) {
					ItemStack stack = var1.getStackInSlot(i);
					if (!stack.isEmpty() && stack.getItem() instanceof RemoteOrderer) {
						result.setTagCompound(stack.getTagCompound());
						break;
					}
				}
				return result;
			}
		}
	}

	private static ResourceLocation getFreeRecipeResourceLocation(ItemStack stack) {
		return getFreeRecipeResourceLocation(stack.getItem());
	}

	private static ResourceLocation getFreeRecipeResourceLocation(Item item) {
		ResourceLocation baseLoc = new ResourceLocation(LPConstants.LP_MOD_ID, item.getRegistryName().getResourcePath());
		ResourceLocation recipeLoc = baseLoc;
		int index = 0;
		while (CraftingManager.REGISTRY.containsKey(recipeLoc)) {
			index++;
			recipeLoc = new ResourceLocation(LPConstants.LP_MOD_ID, baseLoc.getResourcePath() + "_" + index);
		}
		return recipeLoc;
	}
}
