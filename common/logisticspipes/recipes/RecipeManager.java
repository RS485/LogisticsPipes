package logisticspipes.recipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemModule;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.modules.LogisticsModule;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;

public class RecipeManager {
	
	public static void loadRecipes() {
		class LocalCraftingManager {
			private CraftingManager craftingManager = CraftingManager.getInstance();
			@SuppressWarnings("unchecked")
			public void addRecipe(ItemStack stack, CraftingDependency dependent, Object... objects) {
				craftingManager.getRecipeList().add(new LPShapedOreRecipe(stack, dependent, objects));
			}
			@SuppressWarnings("unchecked")
			public void addOrdererRecipe(ItemStack stack, String dye, ItemStack orderer) {
				craftingManager.getRecipeList().add(new ShapelessOreRecipe(stack, new Object[] {dye, orderer}) {
					@Override
					public ItemStack getCraftingResult(InventoryCrafting var1) {
						ItemStack result = super.getCraftingResult(var1);
						for(int i=0;i<var1.getInventoryStackLimit();i++) {
							ItemStack stack = var1.getStackInSlot(i);
							if(stack != null && stack.getItem() instanceof RemoteOrderer) {
								result.setTagCompound(stack.getTagCompound());
								break;
							}
						}
						return result;
					}
				});
			}
			@SuppressWarnings("unchecked")
			public void addShapelessRecipe(ItemStack stack, CraftingDependency dependent, Object... objects) {
				craftingManager.getRecipeList().add(new LPShapelessOreRecipe(stack, dependent, objects));
			}
			@SuppressWarnings("unchecked")
			public void addShapelessResetRecipe(int itemID, int meta) {
				craftingManager.getRecipeList().add(new ShapelessResetRecipe(itemID, meta));
			}
		};
		LocalCraftingManager craftingManager = new LocalCraftingManager();
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk1, 1), CraftingDependency.DistanceRequest, new Object[] {
			"lPl",
			" B ",
			Character.valueOf('l'), "dyeBlue",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('B'), Item.bucketEmpty
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe, 8), CraftingDependency.Basic, new Object[] {
			"grg",
			"cdc",
			" G ",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('g'), Block.glass,
			Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond,
			Character.valueOf('c'), BuildCraftTransport.pipeItemsCobblestone,
			Character.valueOf('r'), Block.torchRedstoneActive
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsBasicPipe, 8), CraftingDependency.Basic, new Object[] {
			"grg",
			"cdc",
			" G ",
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('g'), Block.glass,
			Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond,
			Character.valueOf('c'), BuildCraftTransport.pipeItemsCobblestone,
			Character.valueOf('r'), Block.torchRedstoneActive
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk1, 1), CraftingDependency.Basic, new Object[] {
			" G ",
			"rPr",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk1, 1), CraftingDependency.Basic, new Object[] {
			"G",
			"P",
			"R",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('R'), Block.torchRedstoneActive
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
			Character.valueOf('U'), BuildCraftCore.diamondGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsProviderPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk1, 1), CraftingDependency.Basic, new Object[] {
			"r",
			"P",
			"S",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('S'), BuildCraftCore.stoneGearItem,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSatellitePipe, 1), CraftingDependency.DistanceRequest, new Object[] {
			"rPr",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSupplierPipe, 1), CraftingDependency.DistanceRequest, new Object[] {
			"lPl",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('l'), "dyeBlue"
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk1, 1), CraftingDependency.Basic, new Object[] {
			"g",
			"P",
			"i",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('g'), BuildCraftCore.goldGearItem,
			Character.valueOf('i'), BuildCraftCore.ironGearItem
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk1, 1), CraftingDependency.Basic, new Object[] {
			"g",
			"P",
			"i",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('g'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('i'), BuildCraftCore.ironGearItem
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"U",
			"B",
			"r",
			Character.valueOf('B'), LogisticsPipes.LogisticsRequestPipeMk1,
			Character.valueOf('U'), BuildCraftCore.diamondGearItem,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRequestPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), LogisticsPipes.LogisticsRequestPipeMk1,
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"U",
			"B",
			"r",
			Character.valueOf('B'), LogisticsPipes.LogisticsCraftingPipeMk1,
			Character.valueOf('U'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingPipeMk2, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), LogisticsPipes.LogisticsCraftingPipeMk1,
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrdererPipe, 1), CraftingDependency.Passthrough, new Object[] {
			"U",
			"B",
			"r",
			Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('U'), Item.enderPearl,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsInvSysConPipe, 1), CraftingDependency.Passthrough, new Object[] {
			" E ",
			"rPr",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('E'), Item.enderPearl,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsEntrancePipe, 1), CraftingDependency.Passthrough, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
			Character.valueOf('U'), "dyeGreen"
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsDestinationPipe, 1), CraftingDependency.Passthrough, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), LogisticsPipes.LogisticsProviderPipeMk1,
			Character.valueOf('U'), "dyeRed"
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsItemDisk, 1), CraftingDependency.Fast_Crafting, new Object[] {
			"igi",
			"grg",
			"igi",
			Character.valueOf('i'), "dyeBlack",
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('g'), Item.goldNugget
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), CraftingDependency.Modular_Pipes, new Object[] {
			" p ",
			"rpr",
			" g ",
			Character.valueOf('p'), Item.paper,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('g'), Item.goldNugget
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			"rBr",
			Character.valueOf('C'), "dyeGreen",
			Character.valueOf('G'), BuildCraftCore.ironGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			" B ",
			Character.valueOf('C'), "dyeGreen",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			"rBr",
			Character.valueOf('C'), "dyeRed",
			Character.valueOf('G'), BuildCraftCore.ironGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			" B ",
			Character.valueOf('C'), "dyeRed",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
			"CGC",
			"rBr",
			Character.valueOf('C'), "dyeBlue",
			Character.valueOf('G'), BuildCraftCore.ironGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
			"CGC",
			" B ",
			Character.valueOf('C'), "dyeBlue",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR), CraftingDependency.Active_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
			Character.valueOf('U'), Item.redstone
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
			Character.valueOf('U'), BuildCraftCore.goldGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR),
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
			Character.valueOf('U'), BuildCraftCore.goldGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
			Character.valueOf('U'), Item.redstone
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
			Character.valueOf('U'), BuildCraftCore.diamondGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
			Character.valueOf('U'), BuildCraftCore.diamondGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.EXTRACTOR_MK3),
			Character.valueOf('U'), Item.redstone
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			"rBr",
			Character.valueOf('C'), "dyeOrange",
			Character.valueOf('G'), BuildCraftCore.ironGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			" B ",
			Character.valueOf('C'), "dyeOrange",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.QUICKSORT), CraftingDependency.Active_Modules, new Object[] {
			"CGC",
			"rBr",
			Character.valueOf('C'), "dyeBlue",
			Character.valueOf('G'), BuildCraftCore.diamondGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.QUICKSORT), CraftingDependency.Active_Modules, new Object[] {
			"CGC",
			" B ",
			Character.valueOf('C'), "dyeBlue",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.TERMINUS), CraftingDependency.Modular_Pipes, new Object[] {
			"CGD",
			"rBr",
			Character.valueOf('C'), "dyeBlack",
			Character.valueOf('D'), "dyePurple",
			Character.valueOf('G'), BuildCraftCore.ironGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.TERMINUS), CraftingDependency.Modular_Pipes, new Object[] {
			"CGD",
			" B ",
			Character.valueOf('C'), "dyeBlack",
			Character.valueOf('D'), "dyePurple",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			"rBr",
			Character.valueOf('C'), "dyeBlue",
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER), CraftingDependency.Modular_Pipes, new Object[] {
			"CGC",
			" B ",
			Character.valueOf('C'), "dyeBlue",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER),
			Character.valueOf('U'), BuildCraftCore.diamondGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER_MK2), CraftingDependency.High_Tech_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.PROVIDER),
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK),
			Character.valueOf('U'), BuildCraftCore.goldGearItem
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK),
			Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.OREDICTITEMSINK), CraftingDependency.Sink_Modules, new Object[] {
			"U",
			"B",
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.MODBASEDITEMSINK),
			Character.valueOf('U'), Item.book
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ENCHANTMENTSINK), CraftingDependency.Sink_Modules, new Object[] {
			"E",
			"B",
			Character.valueOf('E'), Item.enchantedBook,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK),
		});

		for(int i=0; i<1000;i++) {
			LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(new ItemStack(LogisticsPipes.ModuleItem, 1, i), null, null, null, null, null);
			if(module != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				boolean force = false;
				try {
					module.writeToNBT(nbt);
				} catch(Exception e) {
					force = true;
				}
				if(!nbt.equals(new NBTTagCompound()) || force) {
					craftingManager.addShapelessResetRecipe(LogisticsPipes.ModuleItem.itemID, i);
				}
			}
		}
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk1, 1), CraftingDependency.Modular_Pipes, new Object[] {
			"iii",
			"uPu",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('u'), Item.ingotIron,
			Character.valueOf('i'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk1, 1), CraftingDependency.Modular_Pipes, new Object[] {
			" i ",
			"uPu",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('u'), Item.ingotIron,
			Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 0)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk2, 1), CraftingDependency.Modular_Pipes, new Object[] {
			"iii",
			"iPi",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('i'), Item.ingotIron
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk2, 1), CraftingDependency.Modular_Pipes, new Object[] {
			" i ",
			"uPu",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('u'), Item.ingotIron,
			Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk3, 1), CraftingDependency.Modular_Pipes, new Object[] {
			"iii",
			"iPi",
			"iii",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('i'), Item.ingotIron
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk3, 1), CraftingDependency.Modular_Pipes, new Object[] {
			" i ",
			"uPu",
			" i ",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('u'), Item.ingotIron,
			Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk4, 1), CraftingDependency.Modular_Pipes, new Object[] {
			"iii",
			"iPi",
			"ggg",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('i'), Item.ingotIron,
			Character.valueOf('g'), Item.ingotGold
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk4, 1), CraftingDependency.Modular_Pipes, new Object[] {
			" i ",
			"uPu",
			" g ",
			Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('u'), Item.ingotIron,
			Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('g'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsChassisPipeMk5, 1), CraftingDependency.Large_Chasie, new Object[] {
			"d",
			"P",
			Character.valueOf('P'), LogisticsPipes.LogisticsChassisPipeMk4,
			Character.valueOf('d'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsNetworkMonitior, 1), CraftingDependency.Basic, new Object[] {
			"g g",
			" G ",
			" g ",
			Character.valueOf('g'), Item.ingotGold,
			Character.valueOf('G'), BuildCraftCore.goldGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsNetworkMonitior, 1), CraftingDependency.Basic, new Object[] {
			"g g",
			" G ",
			" g ",
			Character.valueOf('g'), Item.ingotGold,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, 0), CraftingDependency.DistanceRequest, new Object[] {
			"gg",
			"gg",
			"DD",
			Character.valueOf('g'), Block.glass,
			Character.valueOf('D'), BuildCraftCore.diamondGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, 0), CraftingDependency.DistanceRequest, new Object[] {
			"gg",
			"gg",
			"DD",
			Character.valueOf('g'), Block.glass,
			Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});
		
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
			craftingManager.addOrdererRecipe(new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, i), 
				dyes[i - 1], 
				new ItemStack(LogisticsPipes.LogisticsRemoteOrderer, 1, -1)
				);
			craftingManager.addShapelessResetRecipe(LogisticsPipes.LogisticsRemoteOrderer.itemID, i);
		}
		craftingManager.addShapelessResetRecipe(LogisticsPipes.LogisticsRemoteOrderer.itemID, 0);
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingSignCreator, 1), CraftingDependency.Information_System, new Object[] {
			"G G",
			" S ",
			" D ",
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('S'), Item.sign,
			Character.valueOf('D'), BuildCraftCore.diamondGearItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsCraftingSignCreator, 1), CraftingDependency.Information_System, new Object[] {
			"G G",
			" S ",
			" D ",
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('S'), Item.sign,
			Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 0), CraftingDependency.Basic, new Object[] {
			"iCi",
			"i i",
			"iri",
			Character.valueOf('C'), new ItemStack(Block.workbench, 1),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('i'), Item.ingotIron
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 1), CraftingDependency.Basic, new Object[] {
			"iii",
			"rRr",
			"iii",
			Character.valueOf('R'), Block.blockRedstone,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('i'), Item.ingotIron
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 2), CraftingDependency.Security, new Object[] {
			"iDi",
			"rBr",
			"iii",
			Character.valueOf('D'), new ItemStack(BuildCraftCore.diamondGearItem, 1),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('i'), Item.ingotIron
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 2), CraftingDependency.Security, new Object[] {
			"iDi",
			"rBr",
			"iii",
			Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('i'), Item.ingotIron
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, 3), CraftingDependency.Basic, new Object[] { 
			"wCw", 
			" G ", 
			"wSw", 
			Character.valueOf('w'), "plankWood",
			Character.valueOf('C'), Block.workbench,
			Character.valueOf('S'), Block.chest,
			Character.valueOf('G'), BuildCraftCore.stoneGearItem
		});
		
		craftingManager.addShapelessRecipe(new ItemStack(LogisticsPipes.LogisticsUpgradeManager, 1), CraftingDependency.Upgrades, new Object[] { 
			LogisticsPipes.LogisticsNetworkMonitior,
			BuildCraftCore.wrenchItem
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 0), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"srs", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.slimeBall
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 1), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCr", 
			"srs", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.slimeBall
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 2), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PsP", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.slimeBall
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 3), CraftingDependency.Upgrades, new Object[] {
			false, 
			"PrP", 
			"rCr", 
			"PsP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.slimeBall
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 4), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"sCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.slimeBall
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 5), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCs", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.slimeBall
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 6), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 20), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.ingotGold, 
			Character.valueOf('P'), Item.paper
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 21), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.ingotIron, 
			Character.valueOf('P'), Item.paper
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 22), CraftingDependency.Active_Liquid, new Object[] { 
			false, 
			"RbR", 
			"bCb", 
			"RbR", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('R'), Item.redstone, 
			Character.valueOf('b'), Item.glassBottle, 
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 23), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"RgR", 
			"gCg", 
			"RgR", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('R'), Item.redstone, 
			Character.valueOf('g'), BuildCraftCore.woodenGearItem, 
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 10), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"srs", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.ingotIron
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 11), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCr", 
			"srs", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.ingotIron
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 12), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PsP", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.ingotIron
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 13), CraftingDependency.Upgrades, new Object[] {
			false, 
			"PrP", 
			"rCr", 
			"PsP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.ingotIron
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 14), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"sCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.ingotIron
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 15), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCs", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('P'), Item.paper, 
			Character.valueOf('s'), Item.ingotIron
		});

		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 24), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"Rhy", 
			"iCi", 
			"riR", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('R'), Item.redstone, 
			Character.valueOf('r'), dyes[1], 
			Character.valueOf('y'), dyes[11], 
			Character.valueOf('h'), Block.hopperBlock,
			Character.valueOf('i'), Item.ingotIron, 
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, 25), CraftingDependency.Upgrades, new Object[] { 
			false, 
			"PrP", 
			"rCr", 
			"PrP", 
			Character.valueOf('C'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.netherQuartz, 
			Character.valueOf('P'), Item.paper
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidConnectorPipe, 4), CraftingDependency.Basic_Liquid, new Object[] {
			"GPG",
			"gLg",
			Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
			Character.valueOf('P'), BuildCraftTransport.pipeFluidsGold,
			Character.valueOf('G'), Block.glass,
			Character.valueOf('g'), Item.ingotGold
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidBasicPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
			"w",
			"B",
			"b",
			Character.valueOf('B'), LogisticsPipes.LogisticsBasicPipe,
			Character.valueOf('w'), BuildCraftTransport.pipeWaterproof,
			Character.valueOf('b'), Item.bucketEmpty
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSatellitePipe, 1), CraftingDependency.Active_Liquid, new Object[] {
			"rLr",
			Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
			Character.valueOf('r'), Item.redstone
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidSupplierPipeMk2, 1), CraftingDependency.Active_Liquid, new Object[] {
			" g ",
			"lPl",
			" g ",
			Character.valueOf('l'), "dyeBlue",
			Character.valueOf('P'), LogisticsPipes.LogisticsFluidBasicPipe,
			Character.valueOf('g'), Item.ingotGold
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidInsertionPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
			" g ",
			"gLg",
			" g ",
			Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
			Character.valueOf('g'), Item.glassBottle
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidProviderPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
			"g",
			"L",
			Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
			Character.valueOf('g'), Item.glassBottle
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidRequestPipe, 1), CraftingDependency.Basic_Liquid, new Object[] {
			"gLg",
			Character.valueOf('L'), LogisticsPipes.LogisticsFluidBasicPipe,
			Character.valueOf('g'), Item.glassBottle
		});
		
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsFluidExtractorPipe, 1), CraftingDependency.Active_Liquid, new Object[] {
			"w",
			"I",
			Character.valueOf('I'), LogisticsPipes.LogisticsFluidInsertionPipe,
			Character.valueOf('w'), BuildCraftTransport.pipeFluidsWood
		});
	}
}
