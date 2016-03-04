package logisticspipes.recipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.proxy.interfaces.ICraftingParts;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;
import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS;

//@formatter:off
//CHECKSTYLE:OFF

public class RecipeManager {
	public static class LocalCraftingManager {
		private CraftingManager craftingManager = CraftingManager.getInstance();
		public LocalCraftingManager() {}
		@SuppressWarnings("unchecked")
		public void addRecipe(ItemStack stack, CraftingDependency dependent, Object... objects) {
			craftingManager.getRecipeList().add(new LPShapedOreRecipe(stack, dependent, objects));
		}
		@SuppressWarnings("unchecked")
		public void addOrdererRecipe(ItemStack stack, String dye, ItemStack orderer) {
			craftingManager.getRecipeList().add(new ShapelessOrdererRecipe(stack, new Object[] {dye, orderer}));
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

	public static LocalCraftingManager craftingManager = new LocalCraftingManager();

	public static void registerRecipeClasses() {
		RecipeSorter.register("logisticspipes:shapedore", LPShapedOreRecipe.class, SHAPED, "after:minecraft:shaped before:minecraft:shapeless");
		RecipeSorter.register("logisticspipes:shapelessore", LPShapelessOreRecipe.class, SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("logisticspipes:shapelessreset", ShapelessResetRecipe.class, SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("logisticspipes:shapelessorderer", LocalCraftingManager.ShapelessOrdererRecipe.class, SHAPELESS, "after:minecraft:shapeless");
	}

	public static void loadRecipes(ICraftingParts parts) {


		for(int i=0; i<1000;i++) {
			LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(new ItemStack(LogisticsPipes.ModuleItem, 1, i), null, null, null);
			if(module != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				boolean force = false;
				try {
					module.writeToNBT(nbt);
				} catch(Exception e) {
					force = true;
				}
				if(!nbt.equals(new NBTTagCompound()) || force) {
					RecipeManager.craftingManager.addShapelessResetRecipe(LogisticsPipes.ModuleItem, i);
				}
			}
		}

		String[] dyes =
		{
			"dyeBlack",
			"dyeRed",
			"dyeGreen",
			"dyeBrown",
			"dyeBlue",
			"dyePurple",
			"dyeCyan",
			"dyeLightGray",
			"dyeGray",
			"dyePink",
			"dyeLime",
			"dyeYellow",
			"dyeLightBlue",
			"dyeMagenta",
			"dyeOrange",
			"dyeWhite"
		};
		for(int i=1;i<17;i++) {
			RecipeManager.craftingManager.addOrdererRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, i),
					dyes[i - 1],
					new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, -1)
					);
			RecipeManager.craftingManager.addShapelessResetRecipe(LogisticsPipes.LogisticsRemoteOrderer, i);
		}
		RecipeManager.craftingManager.addShapelessResetRecipe(LogisticsPipes.LogisticsRemoteOrderer, 0);
	}
}
