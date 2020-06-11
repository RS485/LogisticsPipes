package logisticspipes.recipes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.GameData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import logisticspipes.LPConstants;
import logisticspipes.items.RemoteOrderer;

public class RecipeManager {

	public static final List<IRecipeProvider> recipeProvider = new ArrayList<>();
	public static final LocalCraftingManager craftingManager = new LocalCraftingManager();

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
		public void addRecipe(@Nonnull ItemStack stack, Object... objects) {
			List<Object> result = new ArrayList<>();
			final boolean[] addRecipe = { true };
			Arrays.stream(objects).forEach(o -> {
				if (!addRecipe[0]) return;
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
					if (((RecipeIndex) o).getValue() == null) {
						addRecipe[0] = false;
					}
				} else {
					result.add(o);
				}
			});
			if (!addRecipe[0]) return;

			//			RecipeIndex[] indices = new RecipeIndex[objects.length-1];
			//			System.arraycopy(objects, 1, indices, 0, indices.length);
			//			dumpRecipe(stack, objects[0], indices);

			GameData.register_impl(new ShapedOreRecipe(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), stack, result.toArray()).setRegistryName(getFreeRecipeResourceLocation(stack)));
		}

		private void dumpRecipe(@Nonnull ItemStack result, Object layout, RecipeIndex... indices) {
			JsonObject obj = new JsonObject();
			JsonArray pattern = new JsonArray();
			JsonObject keys = new JsonObject();
			obj.addProperty("type", "minecraft:crafting_shaped");

			if (layout instanceof RecipeLayout) {
				pattern.add(((RecipeLayout) layout).getLine1());
				pattern.add(((RecipeLayout) layout).getLine2());
				pattern.add(((RecipeLayout) layout).getLine3());
			} else if (layout instanceof RecipeLayoutSmall) {
				pattern.add(((RecipeLayoutSmall) layout).getLine1());
				pattern.add(((RecipeLayoutSmall) layout).getLine2());
			} else if (layout instanceof RecipeLayoutSmaller) {
				pattern.add(((RecipeLayoutSmaller) layout).getLine1());
			}

			for (RecipeIndex index : indices) {
				JsonObject key = new JsonObject();
				if (index.getValue() instanceof String) {
					key.addProperty("type", "forge:ore_dict");
					key.addProperty("ore", (String) index.getValue());
				} else if (index.getValue() instanceof ItemStack) {
					ItemStack stack = (ItemStack) index.getValue();
					key.addProperty("item", stack.getItem().getRegistryName().toString());
					if (stack.getHasSubtypes()) key.addProperty("data", stack.getItemDamage());
				} else if (index.getValue() instanceof Item) {
					key.addProperty("item", ((Item) index.getValue()).getRegistryName().toString());
				} else if (index.getValue() instanceof Block) {
					key.addProperty("item", Item.getItemFromBlock((Block) index.getValue()).getRegistryName().toString());
					//				} else if (index.getValue() instanceof NBTIngredient) {
					//					key.addProperty("type", "minecraft:item_nbt");
					//					NBTIngredient value = (NBTIngredient) index.getValue();
					//					ItemStack stack = value.getMatchingStacks()[0];
					//					if (value.getMatchingStacks().length > 1) throw new NotImplementedException("valid stacks size > 1");
					//					key.addProperty("item", stack.getItem().getRegistryName().toString());
					//					if (stack.getHasSubtypes()) key.addProperty("data", stack.getItemDamage());
					//					JsonObject nbt = new JsonObject();
					//					// TODO
					//					key.add("nbt", nbt);
				} else {
					System.out.printf("unhandled ingredient type, skipping export (%s)\n", index.getValue());
					return;
				}
				keys.add(index.getIndex() + "", key);
			}

			JsonObject r = new JsonObject();
			r.addProperty("item", result.getItem().getRegistryName().toString());
			if (result.getItemDamage() > 0) r.addProperty("data", result.getItemDamage());
			if (result.getCount() > 1) r.addProperty("count", result.getCount());
			obj.add("result", r);
			obj.add("key", keys);
			obj.add("pattern", pattern);

			String format;
			if (result.getHasSubtypes()) {
				format = String.format("generated_recipes/%s.%d.json", result.getItem().getRegistryName().getResourcePath(), result.getItemDamage());
			} else {
				format = String.format("generated_recipes/%s.json", result.getItem().getRegistryName().getResourcePath());
			}

			File out = new File(format);
			out.getParentFile().mkdirs();
			String text = new Gson().toJson(obj);
			try {
				Files.write(out.toPath(), text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void addOrdererRecipe(@Nonnull ItemStack stack, String dye, ItemStack orderer) {
			GameData.register_impl(new ShapelessOrdererRecipe(getFreeRecipeResourceLocation(stack), stack, dye, orderer));
		}

		public void addShapelessRecipe(@Nonnull ItemStack stack, Object... objects) {
			GameData.register_impl(new ShapelessOreRecipe(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), stack, objects).setRegistryName(getFreeRecipeResourceLocation(stack)));
		}

		public void addShapelessResetRecipe(Item item, int meta) {
			ShapelessResetRecipe value = new ShapelessResetRecipe(item, meta);
			value.setRegistryName(getFreeRecipeResourceLocation(item));
			GameData.register_impl(value);
		}

		public static class ShapelessOrdererRecipe extends ShapelessOreRecipe {

			public ShapelessOrdererRecipe(ResourceLocation registryName, @Nonnull ItemStack result, Object... recipe) {
				super(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), result, recipe);
				setRegistryName(registryName);
			}

			@Nonnull
			@Override
			public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
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

	private static ResourceLocation getFreeRecipeResourceLocation(@Nonnull ItemStack stack) {
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
