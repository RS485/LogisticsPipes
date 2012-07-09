/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import java.io.File;

import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.Trigger;
//import net.minecraft.src.buildcraft.core.Action;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.ActionDisableLogistics;
import net.minecraft.src.buildcraft.krapht.BuildCraftProxy3;
import net.minecraft.src.buildcraft.krapht.IBuildCraftProxy;
import net.minecraft.src.buildcraft.krapht.LogisticsTriggerProvider;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.TriggerSupplierFailed;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsBuilderSupplierLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsLiquidSupplier;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsSupplierLogistics;
import net.minecraft.src.buildcraft.logisticspipes.ItemModule;
import net.minecraft.src.buildcraft.logisticspipes.ModTextureProxy;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.Property;

public class mod_LogisticsPipes extends ModTextureProxy {
	//Triggers
	
	public mod_LogisticsPipes() {
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy3());
		instance = this;
	}
	
	public static mod_LogisticsPipes instance;
	
	public static Trigger LogisticsFailedTrigger;
	
	public static Action LogisticsDisableAction;
	
	//Items
	public static Item LogisticsBuilderSupplierPipe;
	public static Item LogisticsLiquidSupplierPipe;
	
	//Id
	public static int LOGISTICSPIPE_BUILDERSUPPLIER_ID			= 6880;
	public static int LOGISTICSPIPE_LIQUIDSUPPLIER_ID			= 6886;
	
	//Textures
	public static int LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE		= 0;
	public static int LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE		= 0;
	
	//Log Requests
	public static boolean DisplayRequests;
	
	//Texture files
	public static final String LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE			= "/logisticspipes/pipes/builder_supplier.png";
	public static final String LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE			= "/logisticspipes/pipes/liquid_supplier.png";
	
	

	@Override
	public void modsLoaded() {
		super.modsLoaded();
		
		BuildCraftBuilders.initialize();
		BuildCraftSilicon.initialize();
		
		this.LogisticsFailedTrigger = new TriggerSupplierFailed(700);
		BuildCraftAPI.registerTriggerProvider(new LogisticsTriggerProvider());
		
		this.LogisticsDisableAction = new ActionDisableLogistics(700);
		
		Property logisticPipeBuilderSupplierIdProperty = configuration.getOrCreateIntProperty("logisticsPipeBuilderSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_BUILDERSUPPLIER_ID);
		logisticPipeBuilderSupplierIdProperty.comment = "The item id for the builder supplier pipe";
		
		Property logisticPipeLiquidSupplierIdProperty = configuration.getOrCreateIntProperty("logisticsPipeLiquidSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_LIQUIDSUPPLIER_ID);
		logisticPipeLiquidSupplierIdProperty.comment = "The item id for the liquid supplier pipe";

		
		configuration.save();
		
		LOGISTICSPIPE_BUILDERSUPPLIER_ID		= Integer.parseInt(logisticPipeBuilderSupplierIdProperty.value);
		LOGISTICSPIPE_LIQUIDSUPPLIER_ID			= Integer.parseInt(logisticPipeLiquidSupplierIdProperty.value);
		
		LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE);
		
		LogisticsBuilderSupplierPipe = createPipe(LOGISTICSPIPE_BUILDERSUPPLIER_ID, PipeItemsBuilderSupplierLogistics.class, "Builder Supplier Logistics Pipe");
		LogisticsLiquidSupplierPipe = createPipe(LOGISTICSPIPE_LIQUIDSUPPLIER_ID, PipeItemsLiquidSupplier.class, "Liquid Supplier Logistics Pipe");
		
		CraftingManager craftingManager = CraftingManager.getInstance();
		craftingManager.addRecipe(new ItemStack(LogisticsBuilderSupplierPipe, 1), new Object[]{"iPy", Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('y'), new ItemStack(Item.dyePowder, 1,11)});
		craftingManager.addRecipe(new ItemStack(LogisticsLiquidSupplierPipe, 1), new Object[]{" B ", "lPl", " B ", Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('B'), Item.bucketEmpty});
		
		craftingManager.addRecipe(new ItemStack(LogisticsBasicPipe, 8), new Object[] { "grg", "GdG", "grg", Character.valueOf('g'), Block.glass, 
			   Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			   Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond, 
			   Character.valueOf('r'), Block.torchRedstoneActive});
		
		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipe, 1), new Object[] { "gPg", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('g'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsRequestPipe, Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsCraftingPipeMK2, Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		
		//ItemSink
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ITEMSINK), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 2),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		//Passive supplier
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 1),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//Extractor module
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//Polymorphic ItemSink (ChestItemSink)
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 14),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//QuickSort
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.QUICKSORT), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//Terminus
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.TERMINUS), new Object[] { " G ", "rBr", "CrD", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 0),
									Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//Provider
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PROVIDER), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//PASSIVE MK 2
		//Extractor module MK 2
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		
		//Advanced Extractor module MK 2
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		

		//PASSIVE MK 3
		//Extractor module MK 3
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		//Advanced Extractor module MK 3
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		
		//ChassiPipes
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe1, 1), new Object[] { " i ","iPi", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 0)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe2, 1), new Object[] { " i ","iPi", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe3, 1), new Object[] { " i ","iPi", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe4, 1), new Object[] { " i ","iPi", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		if(LOGISTICS_BLOCK_ID != 0) {
			craftingManager.addRecipe(new ItemStack(LogisticsCraftingSignCreater, 1), new Object[] {"G G", " S ", " D ", Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2), Character.valueOf('S'), Item.sign, Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		}
		
		//BuildCraftCore.itemBptProps[core_LogisticsPipes.LogisticsBasicPipe.shiftedIndex] = new bptItemBasicLogisticsPipe();

	}

	@Override
	public String getVersion(){
		return "0.3.1";
	}
}
