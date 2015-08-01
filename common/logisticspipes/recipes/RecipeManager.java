package logisticspipes.recipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemPipeComponents;
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
		if(!Configs.ENABLE_BETA_RECIPES)
		{
			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.BasicTransportPipe, 8),  CraftingDependency.Basic, new Object[] {
				"IgI",
				" r ",
				Character.valueOf('g'), new ItemStack(Blocks.glass_pane, 1),
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe, 8), CraftingDependency.Basic, new Object[] {
				"grg",
				"cdc",
				" G ",
				Character.valueOf('G'), parts.getChipTear2(),
				Character.valueOf('g'), Blocks.glass,
				Character.valueOf('d'), parts.getSortingLogic(),
				Character.valueOf('c'), parts.getBasicTransport(),
				Character.valueOf('r'), Blocks.redstone_torch
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe, 8), CraftingDependency.Basic, new Object[] {
				"grg",
				"cdc",
				" G ",
				Character.valueOf('G'), parts.getGearTear2(),
				Character.valueOf('g'), Blocks.glass,
				Character.valueOf('d'), parts.getSortingLogic(),
				Character.valueOf('c'), parts.getBasicTransport(),
				Character.valueOf('r'), Blocks.redstone_torch
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				" G ",
				"rPr",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('G'), parts.getGearTear2(),
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				"G",
				"P",
				"R",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('G'), parts.getChipTear2(),
				Character.valueOf('R'), Blocks.redstone_torch
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('U'), parts.getGearTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('U'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				"r",
				"P",
				"S",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('S'), "gearStone",
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSatellitePipe, 1), CraftingDependency.DistanceRequest, new Object[] {
				"rPr",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSupplierPipe, 1), CraftingDependency.DistanceRequest, new Object[] {
				"lPl",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('l'), "dyeBlue"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				"g",
				"P",
				"i",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('g'), parts.getGearTear2(),
				Character.valueOf('i'), parts.getGearTear1()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				"g",
				"P",
				"i",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('g'), parts.getChipTear2(),
				Character.valueOf('i'), parts.getGearTear1()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				"r",
				Character.valueOf('B'), LogisticsPipes.LogisticsRequestPipeMk1,
				Character.valueOf('U'), parts.getGearTear3(),
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsRequestPipeMk1,
				Character.valueOf('U'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				"r",
				Character.valueOf('B'), LogisticsPipes.LogisticsCraftingPipeMk1,
				Character.valueOf('U'), parts.getGearTear2(),
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsCraftingPipeMk1,
				Character.valueOf('U'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrdererPipe, 1), CraftingDependency.Passthrough, new Object[] {
				"U",
				"B",
				"r",
				Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('U'), Items.ender_pearl,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsInvSysConPipe, 1), CraftingDependency.Passthrough, new Object[] {
				" E ",
				"rPr",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('E'), Items.ender_pearl,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsEntrancePipe, 1), CraftingDependency.Passthrough, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('U'), "dyeGreen"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsDestinationPipe, 1), CraftingDependency.Passthrough, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('U'), "dyeRed"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsItemDisk, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"igi",
				"grg",
				"igi",
				Character.valueOf('i'), "dyeBlack",
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('g'), Items.gold_nugget
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), CraftingDependency.Modular_Pipes, new Object[] {
				" p ",
				"rpr",
				" g ",
				Character.valueOf('p'), Items.paper,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('g'), Items.gold_nugget
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeGreen",
				Character.valueOf('G'), parts.getGearTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				" B ",
				Character.valueOf('C'), "dyeGreen",
				Character.valueOf('G'), parts.getChipTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeRed",
				Character.valueOf('G'), parts.getGearTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				" B ",
				Character.valueOf('C'), "dyeRed",
				Character.valueOf('G'), parts.getChipTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ACTIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
				" G ",
				"rBr",
				Character.valueOf('G'), parts.getGearTear2(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ACTIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
				"G",
				"B",
				Character.valueOf('G'), parts.getChipTear2(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), parts.getGearTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
				"CGC",
				" B ",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), parts.getChipTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
				Character.valueOf('U'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
				Character.valueOf('U'), parts.getGearTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
				Character.valueOf('U'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
				Character.valueOf('U'), parts.getGearTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
				Character.valueOf('U'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
				Character.valueOf('U'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
				Character.valueOf('U'), parts.getGearTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
				Character.valueOf('U'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
				Character.valueOf('U'), parts.getGearTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
				Character.valueOf('U'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3),
				Character.valueOf('U'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeOrange",
				Character.valueOf('G'), parts.getGearTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				" B ",
				Character.valueOf('C'), "dyeOrange",
				Character.valueOf('G'), parts.getChipTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.QUICKSORT), CraftingDependency.Active_Modules, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), parts.getGearTear3(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.QUICKSORT), CraftingDependency.Active_Modules, new Object[] {
				"CGC",
				" B ",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), parts.getChipTear3(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.TERMINUS), CraftingDependency.Modular_Pipes, new Object[] {
				"CGD",
				"rBr",
				Character.valueOf('C'), "dyeBlack",
				Character.valueOf('D'), "dyePurple",
				Character.valueOf('G'), parts.getGearTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.TERMINUS), CraftingDependency.Modular_Pipes, new Object[] {
				"CGD",
				" B ",
				Character.valueOf('C'), "dyeBlack",
				Character.valueOf('D'), "dyePurple",
				Character.valueOf('G'), parts.getChipTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), parts.getGearTear2(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				" B ",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), parts.getChipTear2(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER),
				Character.valueOf('U'), parts.getGearTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER),
				Character.valueOf('U'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER), CraftingDependency.Modular_Pipes, new Object[] {
				"rGR",
				" B ",
				"G r",
				Character.valueOf('R'), "dyeRed",
				Character.valueOf('G'), "dyeBlue",
				Character.valueOf('G'), parts.getGearTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK),
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER), CraftingDependency.Modular_Pipes, new Object[] {
				" GR",
				" B ",
				"G  ",
				Character.valueOf('R'), "dyeRed",
				Character.valueOf('G'), "dyeBlue",
				Character.valueOf('G'), parts.getChipTear1(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK2), CraftingDependency.Modular_Pipes, new Object[] {
				new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER), parts.getGearTear2()
			});

			RecipeManager.craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK2), CraftingDependency.Modular_Pipes, new Object[] {
				new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK3), CraftingDependency.Modular_Pipes, new Object[] {
				new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK2), new ItemStack(LogisticsPipes.LogisticsParts, 1, 3)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK),
				Character.valueOf('U'), parts.getGearTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK),
				Character.valueOf('U'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.OREDICTITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK),
				Character.valueOf('U'), Items.book
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CREATIVETABBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.OREDICTITEMSINK),
				Character.valueOf('U'), parts.getGearTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CREATIVETABBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.OREDICTITEMSINK),
				Character.valueOf('U'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK), CraftingDependency.Sink_Modules, new Object[] {
				"E",
				"B",
				Character.valueOf('E'), Items.enchanted_book,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK_MK2), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('U'), parts.getChipTear2(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK)
			});
			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK_MK2), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('U'), parts.getGearTear2(),
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk1, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"uPu",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('u'), Items.iron_ingot,
				Character.valueOf('i'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk2, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"iPi",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk2, 1), CraftingDependency.Modular_Pipes, new Object[] {
				" i ",
				"uPu",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('u'), Items.iron_ingot,
				Character.valueOf('i'), parts.getChipTear1()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk3, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"iPi",
				"iii",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk3, 1), CraftingDependency.Modular_Pipes, new Object[] {
				" i ",
				"uPu",
				" i ",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('u'), Items.iron_ingot,
				Character.valueOf('i'), parts.getChipTear1()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk4, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"iPi",
				"ggg",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot,
				Character.valueOf('g'), Items.gold_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk4, 1), CraftingDependency.Modular_Pipes, new Object[] {
				" i ",
				"uPu",
				" g ",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('u'), Items.iron_ingot,
				Character.valueOf('i'), parts.getChipTear1(),
				Character.valueOf('g'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk5, 1), CraftingDependency.Large_Chasie, new Object[] {
				"d",
				"P",
				Character.valueOf('P'), LogisticsPipes.LogisticsChassisPipeMk4,
				Character.valueOf('d'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeControllerItem, 1), CraftingDependency.Basic, new Object[] {
				"g g",
				" G ",
				" g ",
				Character.valueOf('g'), Items.gold_ingot,
				Character.valueOf('G'), parts.getGearTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeControllerItem, 1), CraftingDependency.Basic, new Object[] {
				"g g",
				" G ",
				" g ",
				Character.valueOf('g'), Items.gold_ingot,
				Character.valueOf('G'), parts.getChipTear2()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, 0), CraftingDependency.DistanceRequest, new Object[] {
				"gg",
				"gg",
				"DD",
				Character.valueOf('g'), Blocks.glass,
				Character.valueOf('D'), parts.getGearTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, 0), CraftingDependency.DistanceRequest, new Object[] {
				"gg",
				"gg",
				"DD",
				Character.valueOf('g'), Blocks.glass,
				Character.valueOf('D'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingSignCreator, 1), CraftingDependency.Information_System, new Object[] {
				"G G",
				" S ",
				" D ",
				Character.valueOf('G'), parts.getGearTear2(),
				Character.valueOf('S'), Items.sign,
				Character.valueOf('D'), parts.getGearTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingSignCreator, 1), CraftingDependency.Information_System, new Object[] {
				"G G",
				" S ",
				" D ",
				Character.valueOf('G'), parts.getChipTear2(),
				Character.valueOf('S'), Items.sign,
				Character.valueOf('D'), parts.getChipTear3()
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 0), CraftingDependency.Basic, new Object[] {
				"iCi",
				"i i",
				"iri",
				Character.valueOf('C'), new ItemStack(Blocks.crafting_table, 1),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 1), CraftingDependency.Basic, new Object[] {
				"iii",
				"rRr",
				"iii",
				Character.valueOf('R'), Blocks.redstone_block,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 2), CraftingDependency.Security, new Object[] {
				"iDi",
				"rBr",
				"iii",
				Character.valueOf('D'), parts.getGearTear3(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 2), CraftingDependency.Security, new Object[] {
				"iDi",
				"rBr",
				"iii",
				Character.valueOf('D'), parts.getChipTear3(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 3), CraftingDependency.Basic, new Object[] {
				"wCw",
				" G ",
				"wSw",
				Character.valueOf('w'), "plankWood",
				Character.valueOf('C'), Blocks.crafting_table,
				Character.valueOf('S'), Blocks.chest,
				Character.valueOf('G'), "gearStone"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_FUZZYCRAFTING_TABLE), CraftingDependency.Basic, new Object[] {
				"Q",
				"T",
				Character.valueOf('T'), new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE),
				Character.valueOf('Q'), Items.quartz
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_STATISTICS_TABLE), CraftingDependency.Advanced_Information, new Object[] {
				"iDi",
				"rBr",
				"iii",
				Character.valueOf('D'), parts.getGearTear2(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_STATISTICS_TABLE), CraftingDependency.Advanced_Information, new Object[] {
				"iDi",
				"rBr",
				"iii",
				Character.valueOf('D'), parts.getChipTear2(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_UP), CraftingDependency.Upgrades, new Object[] {
				false,
				"srs",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_DOWN), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"srs",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_NORTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PsP",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_SOUTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PsP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_EAST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"sCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_WEST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCs",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_COMBINATION), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SPEED), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.gold_ingot,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.ADVANCED_SAT_CRAFTINGPIPE), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear2(),
				Character.valueOf('r'), Items.iron_ingot,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.LIQUID_CRAFTING), CraftingDependency.Active_Liquid, new Object[] {
				false,
				"RbR",
				"bCb",
				"RbR",
				Character.valueOf('C'), parts.getChipTear2(),
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('b'), Items.glass_bottle,
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CRAFTING_BYPRODUCT_EXTRACTOR), CraftingDependency.Upgrades, new Object[] {
				false,
				"RgR",
				"gCg",
				"RgR",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('g'), "gearWood"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_UP), CraftingDependency.Upgrades, new Object[] {
				false,
				"srs",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_DOWN), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"srs",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_NORTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PsP",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_SOUTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PsP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_EAST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"sCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_WEST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCs",
				"PrP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SUPPLIER_PATTERN), CraftingDependency.Upgrades, new Object[] {
				false,
				"Rhy",
				"iCi",
				"riR",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('r'), "dyeRed",
				Character.valueOf('y'), "dyeYellow",
				Character.valueOf('h'), Blocks.hopper,
				Character.valueOf('i'), Items.iron_ingot,
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.FUZZY_CRAFTING), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), parts.getChipTear2(),
				Character.valueOf('r'), Items.quartz,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 4, ItemUpgrade.POWER_TRANSPORTATION), CraftingDependency.Power_Distribution, new Object[] {
				false,
				"PRP",
				"CGC",
				"PLP",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('R'), Blocks.redstone_block,
				Character.valueOf('G'), Blocks.glowstone,
				Character.valueOf('L'), Blocks.lapis_block,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CRAFTING_MONITORING), CraftingDependency.Upgrades, new Object[] {
				false,
				"RLR",
				"aCb",
				"RPR",
				Character.valueOf('C'), parts.getChipTear3(),
				Character.valueOf('P'), new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1, 0),
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('L'), "dyeBlue",
				Character.valueOf('a'), "dyeGreen",
				Character.valueOf('b'), "dyeYellow"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.OPAQUE_UPGRADE), CraftingDependency.Upgrades, new Object[] {
				false,
				"RbR",
				"bCb",
				"RbR",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('b'), "dyeWhite",
			});
			
			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CRAFTING_CLEANUP), CraftingDependency.Upgrades, new Object[] {
				false,
				"RCR",
				"bDb",
				"RbR",
				Character.valueOf('C'), parts.getChipTear1(),
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('D'), "gemDiamond",
				Character.valueOf('b'), "dyeRed",
			});
			

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidBasicPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				"w",
				"B",
				"b",
				Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('w'), parts.getWaterProof(),
				Character.valueOf('b'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk1, 1), CraftingDependency.DistanceRequest, new Object[] {
				"lPl",
				" B ",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
				Character.valueOf('B'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSatellitePipe, 1), CraftingDependency.Active_Liquid, new Object[] {
				"rLr",
				Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk2, 1), CraftingDependency.Active_Liquid, new Object[] {
				" g ",
				"lPl",
				" g ",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('g'), Items.gold_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidInsertionPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				" g ",
				"gLg",
				" g ",
				Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('g'), Items.glass_bottle
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidProviderPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				"g",
				"L",
				Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('g'), Items.glass_bottle
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidRequestPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				"gLg",
				Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('g'), Items.glass_bottle
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidExtractorPipe, 1), CraftingDependency.Active_Liquid, new Object[] {
				"w",
				"I",
				Character.valueOf('I'), LogisticsPipes.LogisticsFluidInsertionPipe,
				Character.valueOf('w'), parts.getExtractorFluid()
			});
		}
		if(Configs.ENABLE_BETA_RECIPES)
		{
			ItemStack micserv = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_MICROSERVO);
			ItemStack logproc = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_ROUTEPROCESSOR);
			ItemStack packager = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_MICROPACKAGER);
			ItemStack capsler = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_MICROCAPSULATOR);
			ItemStack expand = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_LOGICEXPANDER);
			ItemStack lense = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_FOCUSLENSE);
			ItemStack accept = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_POWERACCEPT);
			ItemStack basic = new ItemStack(LogisticsPipes.BasicTransportPipe, 1);
			ItemStack pipe = new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1);

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_PIPESTRUCTURE),  CraftingDependency.Basic, new Object[] {
				"I I",
				"   ",
				"I I",
				Character.valueOf('I'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.BasicTransportPipe, 8),  CraftingDependency.Basic, new Object[] {
				"gSg",
				" r ",
				Character.valueOf('g'), new ItemStack(Blocks.glass_pane, 1),
				Character.valueOf('S'), new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_PIPESTRUCTURE),
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 8, ItemPipeComponents.ITEM_MICROSERVO),  CraftingDependency.Basic, new Object[] {
				"IrI",
				"rIr",
				"IrI",
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 8, ItemPipeComponents.ITEM_MICROPACKAGER),  CraftingDependency.Basic, new Object[] {
				"rPr",
				"I I",
				"IrI",
				Character.valueOf('P'), new ItemStack(Blocks.sticky_piston, 1),
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 8, ItemPipeComponents.ITEM_MICROCAPSULATOR),  CraftingDependency.Basic, new Object[] {
				"rPr",
				"IGI",
				"IrI",
				Character.valueOf('P'), new ItemStack(Blocks.sticky_piston, 1),
				Character.valueOf('G'), Items.glass_bottle,
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_ROUTEPROCESSOR),  CraftingDependency.Basic, new Object[] {
				"nrn",
				"rDr",
				"nrn",
				Character.valueOf('n'), Items.gold_nugget,
				Character.valueOf('D'), Items.diamond,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 2, ItemPipeComponents.ITEM_LOGICEXPANDER),  CraftingDependency.Basic, new Object[] {
				"nrn",
				"rIr",
				"nrn",
				Character.valueOf('n'), Items.gold_nugget,
				Character.valueOf('I'), Items.iron_ingot,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 2, ItemPipeComponents.ITEM_FOCUSLENSE),  CraftingDependency.Basic, new Object[] {
				" g ",
				"ggg",
				" g ",
				Character.valueOf('g'), Blocks.glass
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_POWERACCEPT),  CraftingDependency.Basic, new Object[] {
				"R  ",
				"LRI",
				"RII",
				Character.valueOf('L'), lense,
				Character.valueOf('I'), "nuggetIron",
				Character.valueOf('R'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe, 8), CraftingDependency.Basic, new Object[] {
				"ppp",
				"plp",
				"ppp",
				Character.valueOf('l'), logproc,
				Character.valueOf('p'), basic
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				" p ",
				"rPr",
				" m ",
				Character.valueOf('P'), pipe,
				Character.valueOf('p'), packager,
				Character.valueOf('m'), micserv,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				" r ",
				"pPm",
				Character.valueOf('P'), pipe,
				Character.valueOf('p'), packager,
				Character.valueOf('m'), micserv,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSatellitePipe, 1), CraftingDependency.DistanceRequest, new Object[] {
				" y ",
				"rPr",
				" p ",
				Character.valueOf('y'), "dyeYellow",
				Character.valueOf('P'), pipe,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('p'), packager
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSupplierPipe, 1), CraftingDependency.DistanceRequest, new Object[] {
				"rPr",
				" p ",
				Character.valueOf('r'), "dyeBlue",
				Character.valueOf('P'), pipe,
				Character.valueOf('p'), packager
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk1, 1), CraftingDependency.Basic, new Object[] {
				"g",
				"P",
				"i",
				Character.valueOf('P'), pipe,
				Character.valueOf('g'), logproc,
				Character.valueOf('i'), packager
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				"r",
				Character.valueOf('B'), LogisticsPipes.LogisticsRequestPipeMk1,
				Character.valueOf('U'), logproc,
				Character.valueOf('r'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"U",
				"B",
				"r",
				Character.valueOf('B'), LogisticsPipes.LogisticsCraftingPipeMk1,
				Character.valueOf('U'), expand,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrdererPipe, 1), CraftingDependency.Passthrough, new Object[] {
				"U",
				"B",
				"r",
				Character.valueOf('B'), pipe,
				Character.valueOf('U'), Items.ender_pearl,
				Character.valueOf('r'), packager
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsInvSysConPipe, 1), CraftingDependency.Passthrough, new Object[] {
				" E ",
				"rPr",
				" p ",
				Character.valueOf('P'), pipe,
				Character.valueOf('E'), Items.ender_pearl,
				Character.valueOf('p'), packager,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsEntrancePipe, 1), CraftingDependency.Passthrough, new Object[] {
				"U",
				"B",
				"P",
				Character.valueOf('U'), "dyeGreen",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('P'), packager

			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsDestinationPipe, 1), CraftingDependency.Passthrough, new Object[] {
				"U",
				"B",
				"P",
				Character.valueOf('U'), "dyeRed",
				Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
				Character.valueOf('P'), packager
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsItemDisk, 1), CraftingDependency.Fast_Crafting, new Object[] {
				"igi",
				"grg",
				"igi",
				Character.valueOf('i'), "dyeBlack",
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('g'), Items.gold_nugget
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), CraftingDependency.Modular_Pipes, new Object[] {
				" p ",
				"rpr",
				" g ",
				Character.valueOf('p'), Items.paper,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('g'), Items.gold_nugget
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeGreen",
				Character.valueOf('G'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeRed",
				Character.valueOf('G'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ACTIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
				new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
				"CGC",
				"rBr",
				" s ",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('s'), micserv,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
				Character.valueOf('U'), micserv
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
				Character.valueOf('U'), micserv
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
				Character.valueOf('U'), micserv
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
				Character.valueOf('U'), micserv
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3),
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeOrange",
				Character.valueOf('G'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.QUICKSORT), CraftingDependency.Active_Modules, new Object[] {
				"CGC",
				"rBr",
				" P ",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), logproc,
				Character.valueOf('P'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.TERMINUS), CraftingDependency.Modular_Pipes, new Object[] {
				"CGD",
				"rBr",
				Character.valueOf('C'), "dyeBlack",
				Character.valueOf('D'), "dyePurple",
				Character.valueOf('G'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER), CraftingDependency.Modular_Pipes, new Object[] {
				"CGC",
				"rBr",
				Character.valueOf('C'), "dyeBlue",
				Character.valueOf('G'), packager,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER),
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER), CraftingDependency.Modular_Pipes, new Object[] {
				"rGR",
				" B ",
				"D r",
				Character.valueOf('R'), "dyeRed",
				Character.valueOf('D'), "dyeBlue",
				Character.valueOf('G'), packager,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK),
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK2), CraftingDependency.Modular_Pipes, new Object[] {
				new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER), expand
			});

			RecipeManager.craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK3), CraftingDependency.Modular_Pipes, new Object[] {
				new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CRAFTER_MK2), new ItemStack(LogisticsPipes.LogisticsParts, 1, 3)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK),
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.OREDICTITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK),
				Character.valueOf('U'), Items.book
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.CREATIVETABBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.OREDICTITEMSINK),
				Character.valueOf('U'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK), CraftingDependency.Sink_Modules, new Object[] {
				"E",
				"B",
				Character.valueOf('E'), Items.enchanted_book,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK_MK2), CraftingDependency.Sink_Modules, new Object[] {
				"U",
				"B",
				Character.valueOf('U'), expand,
				Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK)
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk1, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"uPu",
				Character.valueOf('P'), pipe,
				Character.valueOf('u'), Items.iron_ingot,
				Character.valueOf('i'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk2, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"iPi",
				Character.valueOf('P'), pipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk3, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"iPi",
				"iii",
				Character.valueOf('P'), pipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk4, 1), CraftingDependency.Modular_Pipes, new Object[] {
				"iii",
				"iPi",
				"ggg",
				Character.valueOf('P'), pipe,
				Character.valueOf('i'), Items.iron_ingot,
				Character.valueOf('g'), Items.gold_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk5, 1), CraftingDependency.Large_Chasie, new Object[] {
				"d",
				"P",
				Character.valueOf('P'), LogisticsPipes.LogisticsChassisPipeMk4,
				Character.valueOf('d'), logproc
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsPipeControllerItem, 1), CraftingDependency.Basic, new Object[] {
				"g g",
				" G ",
				" g ",
				Character.valueOf('g'), Items.gold_ingot,
				Character.valueOf('G'), logproc
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, 0), CraftingDependency.DistanceRequest, new Object[] {
				"gg",
				"gg",
				"DD",
				Character.valueOf('g'), Blocks.glass,
				Character.valueOf('D'), logproc
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingSignCreator, 1), CraftingDependency.Information_System, new Object[] {
				"G G",
				" S ",
				" D ",
				Character.valueOf('G'), logproc,
				Character.valueOf('S'), Items.sign,
				Character.valueOf('D'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.SOLDERING_STATION), CraftingDependency.Basic, new Object[] {
				"iCi",
				"i i",
				"iri",
				Character.valueOf('C'), new ItemStack(Blocks.crafting_table, 1),
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_POWER_JUNCTION), CraftingDependency.Basic, new Object[] {
				"iii",
				"rRr",
				"iii",
				Character.valueOf('R'), Blocks.redstone_block,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_SECURITY_STATION), CraftingDependency.Security, new Object[] {
				"iDi",
				"rBr",
				"iii",
				Character.valueOf('D'), logproc,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), pipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE), CraftingDependency.Basic, new Object[] {
				"wCw",
				" G ",
				"wSw",
				Character.valueOf('w'), "plankWood",
				Character.valueOf('C'), Blocks.crafting_table,
				Character.valueOf('S'), Blocks.chest,
				Character.valueOf('G'), expand
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_FUZZYCRAFTING_TABLE), CraftingDependency.Basic, new Object[] {
				"Q",
				"T",
				Character.valueOf('T'), new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE),
				Character.valueOf('Q'), Items.quartz
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_STATISTICS_TABLE), CraftingDependency.Advanced_Information, new Object[] {
				"iDi",
				"rBr",
				"iii",
				Character.valueOf('D'), Items.gold_ingot,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('B'), pipe,
				Character.valueOf('i'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_UP), CraftingDependency.Upgrades, new Object[] {
				false,
				"srs",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_DOWN), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"srs",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_NORTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PsP",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_SOUTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PsP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_EAST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"sCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_WEST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCs",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), "slimeball"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SNEAKY_COMBINATION), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SPEED), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.gold_ingot,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.ADVANCED_SAT_CRAFTINGPIPE), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.iron_ingot,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.LIQUID_CRAFTING), CraftingDependency.Active_Liquid, new Object[] {
				false,
				"RbR",
				"bCb",
				"RbR",
				Character.valueOf('C'), capsler,
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('b'), Items.glass_bottle,
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CRAFTING_BYPRODUCT_EXTRACTOR), CraftingDependency.Upgrades, new Object[] {
				false,
				"RgR",
				"gCg",
				"RgR",
				Character.valueOf('C'), expand,
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('g'), "plankWood",
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_UP), CraftingDependency.Upgrades, new Object[] {
				false,
				"srs",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_DOWN), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"srs",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_NORTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PsP",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_SOUTH), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PsP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_WEST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"sCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CONNECTION_EAST), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCs",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.redstone,
				Character.valueOf('P'), Items.paper,
				Character.valueOf('s'), Items.iron_ingot
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.SUPPLIER_PATTERN), CraftingDependency.Upgrades, new Object[] {
				false,
				"Rhy",
				"iCi",
				"riR",
				Character.valueOf('r'), "dyeRed",
				Character.valueOf('y'), "dyeYellow",
				Character.valueOf('C'), expand,
				Character.valueOf('R'), Items.redstone,
				Character.valueOf('h'), Blocks.hopper,
				Character.valueOf('i'), Items.iron_ingot,
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.FUZZY_CRAFTING), CraftingDependency.Upgrades, new Object[] {
				false,
				"PrP",
				"rCr",
				"PrP",
				Character.valueOf('C'), expand,
				Character.valueOf('r'), Items.quartz,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 4, ItemUpgrade.POWER_TRANSPORTATION), CraftingDependency.Power_Distribution, new Object[] {
				false,
				"PGP",
				"RCR",
				"PRP",
				Character.valueOf('C'), expand,
				Character.valueOf('R'), lense,
				Character.valueOf('G'), Blocks.glowstone,
				Character.valueOf('P'), Items.paper
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CRAFTING_MONITORING), CraftingDependency.Upgrades, new Object[] {
				false,
				"RLR",
				"aCb",
				"RPR",
				Character.valueOf('L'), "dyeBlue",
				Character.valueOf('a'), "dyeGreen",
				Character.valueOf('b'), "dyeYellow",
				Character.valueOf('C'), logproc,
				Character.valueOf('P'), new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1, 0),
				Character.valueOf('R'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.OPAQUE_UPGRADE), CraftingDependency.Upgrades, new Object[] {
				false,
				" b ",
				"bIb",
				" b ",
				Character.valueOf('b'), "dyeWhite",
				Character.valueOf('I'), Items.iron_ingot
			});
			
			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.CRAFTING_CLEANUP), CraftingDependency.Upgrades, new Object[] {
				false,
				" b ",
				"bDb",
				" b ",
				Character.valueOf('b'), "dyeRed",
				Character.valueOf('D'), "gemDiamond"
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidBasicPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				" l ",
				"lPl",
				" l ",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), pipe
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk1, 1), CraftingDependency.DistanceRequest, new Object[] {
				"lPl",
				" b ",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), pipe,
				Character.valueOf('b'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSatellitePipe, 1), CraftingDependency.Active_Liquid, new Object[] {
				" r ",
				"rPr",
				" c ",
				Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('c'), capsler,
				Character.valueOf('r'), Items.redstone
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk2, 1), CraftingDependency.Active_Liquid, new Object[] {
				" l ",
				"cPb",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('c'), capsler,
				Character.valueOf('b'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidInsertionPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				" c ",
				"lPl",
				" b ",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('c'), capsler,
				Character.valueOf('b'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidProviderPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				" b ",
				"lPl",
				" c ",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('c'), capsler,
				Character.valueOf('b'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidRequestPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
				" l ",
				"bPc",
				Character.valueOf('l'), "dyeBlue",
				Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('c'), capsler,
				Character.valueOf('b'), Items.bucket
			});

			RecipeManager.craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidExtractorPipe, 1), CraftingDependency.Active_Liquid, new Object[] {
				"w",
				"I",
				"c",
				Character.valueOf('I'), LogisticsPipes.LogisticsFluidBasicPipe,
				Character.valueOf('w'), micserv,
				Character.valueOf('c'), capsler
			});
		}

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
