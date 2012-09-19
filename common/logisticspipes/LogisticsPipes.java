/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

/*
TODO later, maybe....
 - Status screen (in transit, waiting for craft, ready etc)
 - RoutedEntityItem, targetTile - specify which "chest" it should be delivered to
 - RoutedEntityItem, travel time
 - Change recipes to chip-sets in 3.0.0.0
 - Add in-game item for network management (turn on/off link detection, poke link detection etc) ?
 - Context sensitive textures. Flashing routers on deliveries?
 - Track deliveries / en route ?
 - Save stuff, like destinations
 - Texture improvement
 - Route liquids (in container)?
 - Persistance:
 	- Save logistics to file. Save coordinates so they can be resolved later. Also save items in transit and count them as not delivered
 - SMP:
	- Peering, transport other peoples items. Need hook to set owner of PassiveEntity
*/

package logisticspipes;

import logisticspipes.blocks.LogisticsSignBlock;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.routing.ILogisticsManager;
import logisticspipes.items.CraftingSignCreator;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemHUDParts;
import logisticspipes.items.ItemModule;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.main.LogisticsItem;
import logisticspipes.main.LogisticsManager;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic2.ElectricItemProxy;
import logisticspipes.proxy.interfaces.IElectricItemProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.proxy.recipeproviders.SolderingStation;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.ticks.PacketBufferHandlerThread;
import logisticspipes.ticks.RenderTickHandler;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "LogisticsPipes|Main", name = "Logistics Pipes", version = "%VERSION%", dependencies = "required-after:BuildCraft|Transport;required-after:BuildCraft|Builders;required-after:BuildCraft|Silicon;after:IC2;after:Forestry", useMetadata = true)
@NetworkMod(channels = {NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class LogisticsPipes {
	
	@Instance("LogisticsPipes|Main")
	public static LogisticsPipes instance;

	//Log Requests
	public static boolean DisplayRequests;

	public static boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");

	// Items
	public static Item LogisticsBasicPipe;
	public static Item LogisticsRequestPipe;
	public static Item LogisticsProviderPipe;
	public static Item LogisticsCraftingPipe;
	public static Item LogisticsSatellitePipe;
	public static Item LogisticsSupplierPipe;
	public static Item LogisticsBuilderSupplierPipe;
	public static Item LogisticsLiquidSupplierPipe;
	public static Item LogisticsChassiPipe1;
	public static Item LogisticsChassiPipe2;
	public static Item LogisticsChassiPipe3;
	public static Item LogisticsChassiPipe4;
	public static Item LogisticsChassiPipe5;
	public static Item LogisticsCraftingPipeMK2;
	public static Item LogisticsRequestPipeMK2;
	public static Item LogisticsProviderPipeMK2;
	public static Item LogisticsRemoteOrdererPipe;
	public static Item LogisticsApiaristAnalyserPipe;
	public static Item LogisticsApiaristSinkPipe;
	public static Item LogisticsInvSysCon;
	
	
	public static Item LogisticsNetworkMonitior;
	public static Item LogisticsRemoteOrderer;
	public static Item LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static Item LogisticsItemCard;
	public static ItemHUDArmor LogisticsHUDArmor;
	public static Item LogisticsHUDParts;
	
	public static ItemModule ModuleItem;
	
	private Textures textures = new Textures();
	
	//Blocks
	Block logisticsSign;
	Block logisticsSolidBlock;
	
	@Deprecated
	public static ILogisticsManager logisticsManager = new LogisticsManager();

	@Init
	public void init(FMLInitializationEvent event) {
		
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy());
		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManagerV2());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		
		textures.load(event);
		if(event.getSide().isClient()) {
			SimpleServiceLocator.buildCraftProxy.registerLocalization();
		}
		NetworkRegistry.instance().registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		if(event.getSide().equals(Side.CLIENT) && DEBUG) {
			//WIP (highly alpha)
			TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
		}
		if(event.getSide() == Side.CLIENT) {
			new PacketBufferHandlerThread(Side.CLIENT);
			new PacketBufferHandlerThread(Side.SERVER);	
		} else {
			new PacketBufferHandlerThread(Side.SERVER);	
		}
	}
	
	@PreInit
	public void LoadConfig(FMLPreInitializationEvent evt) {
		Configs.load();
	}
	
	@PostInit
	public void PostLoad(FMLPostInitializationEvent event) {
		if(Loader.isModLoaded("Forestry")) {
			SimpleServiceLocator.setForestryProxy(new ForestryProxy());
			System.out.println("Loaded ForestryProxy");
		} else {
			//DummyProxy
			SimpleServiceLocator.setForestryProxy(new IForestryProxy() {
				@Override public boolean isBee(ItemStack item) {return false;}
				@Override public boolean isBee(ItemIdentifier item) {return false;}
				@Override public boolean isAnalysedBee(ItemStack item) {return false;}
				@Override public boolean isAnalysedBee(ItemIdentifier item) {return false;}
				@Override public boolean isTileAnalyser(TileEntity tile) {return false;}
				@Override public boolean forestryEnabled() {return false;}
				@Override public boolean isKnownAlleleId(String uid, World world) {return false;}
				@Override public String getAlleleName(String uid) {return "";}
				@Override public String getFirstAlleleId(ItemStack bee) {return "";}
				@Override public String getSecondAlleleId(ItemStack bee) {return "";}
				@Override public boolean isDrone(ItemStack bee) {return false;}
				@Override public boolean isFlyer(ItemStack bee) {return false;}
				@Override public boolean isPrincess(ItemStack bee) {return false;}
				@Override public boolean isQueen(ItemStack bee) {return false;}
				@Override public boolean isPurebred(ItemStack bee) {return false;}
				@Override public boolean isNocturnal(ItemStack bee) {return false;}
				@Override public boolean isPureNocturnal(ItemStack bee) {return false;}
				@Override public boolean isPureFlyer(ItemStack bee) {return false;}
				@Override public boolean isCave(ItemStack bee) {return false;}
				@Override public boolean isPureCave(ItemStack bee) {return false;}
				@Override public String getForestryTranslation(String input) {return input.substring(input.lastIndexOf(".") + 1).toLowerCase().replace("_", " ");}
				@Override public int getIconIndexForAlleleId(String id, int phase) {return 0;}
				@Override public int getColorForAlleleId(String id, int phase) {return 0;}
				@Override public int getRenderPassesForAlleleId(String id) {return 0;}
				@Override public void addCraftingRecipes() {}
				@Override public String getNextAlleleId(String uid, World world) {return null;}
				@Override public String getPrevAlleleId(String uid, World world) {return null;}
			});
			System.out.println("Loaded Forestry DummyProxy");
		}
		if(Loader.isModLoaded("IC2")) {
			SimpleServiceLocator.setElectricItemProxy(new ElectricItemProxy());
			System.out.println("Loaded IC2Proxy");
		} else {
			//DummyProxy
			SimpleServiceLocator.setElectricItemProxy(new IElectricItemProxy() {
				@Override public boolean isElectricItem(ItemStack stack) {return false;}
				@Override public int getCharge(ItemStack stack) {return 0;}
				@Override public int getMaxCharge(ItemStack stack) {return 0;}
				@Override public boolean isDischarged(ItemStack stack, boolean partial) {return false;}
				@Override public boolean isCharged(ItemStack stack, boolean partial) {return false;}
				@Override public boolean isDischarged(ItemStack stack, boolean partial, Item electricItem) {return false;}
				@Override public boolean isCharged(ItemStack stack, boolean partial, Item electricItem) {return false;}
				@Override public void addCraftingRecipes() {}
			});
			System.out.println("Loaded IC2 DummyProxy");
		}
		SimpleServiceLocator.buildCraftProxy.registerTeleportPipes();
				
		LogisticsNetworkMonitior = new LogisticsItem(Configs.LOGISTICSNETWORKMONITOR_ID);
		LogisticsNetworkMonitior.setIconIndex(Textures.LOGISTICSNETWORKMONITOR_ICONINDEX);
		LogisticsNetworkMonitior.setItemName("networkMonitorItem");
		
		if(DEBUG) {
			LogisticsItemCard = new LogisticsItem(Configs.ItemCardId);
			LogisticsItemCard.setIconIndex(Textures.LOGISTICSITEMCARD_ICONINDEX);
			LogisticsItemCard.setItemName("logisticsItemCard");
			//LogisticsItemCard.setTabToDisplayOn(CreativeTabs.tabRedstone);
		}
		
		LogisticsRemoteOrderer = new RemoteOrderer(Configs.LOGISTICSREMOTEORDERER_ID);
		//LogisticsRemoteOrderer.setIconIndex(LOGISTICSREMOTEORDERER_ICONINDEX);
		LogisticsRemoteOrderer.setItemName("remoteOrdererItem");

		LogisticsCraftingSignCreator = new CraftingSignCreator(Configs.LOGISTICSCRAFTINGSIGNCREATOR_ID);
		LogisticsCraftingSignCreator.setIconIndex(Textures.LOGISTICSCRAFTINGSIGNCREATOR_ICONINDEX);
		LogisticsCraftingSignCreator.setItemName("CraftingSignCreator");
		
		if(DEBUG) {
			int renderIndex;
			if(MainProxy.isClient()) {
				renderIndex = RenderingRegistry.addNewArmourRendererPrefix("LogisticsHUD");
			} else {
				renderIndex = 0;
			}
			LogisticsHUDArmor = new ItemHUDArmor(Configs.ItemHUDId, renderIndex);
			LogisticsHUDArmor.setIconIndex(Textures.LOGISTICSITEMHUD_ICONINDEX);
			LogisticsHUDArmor.setItemName("logisticsHUDGlasses");
			
			LogisticsHUDParts = new ItemHUDParts(Configs.ItemHUDPartsId);
			LogisticsHUDParts.setIconIndex(Textures.LOGISTICSITEMHUD_PART3_ICONINDEX);
			LogisticsHUDParts.setItemName("logisticsHUDParts");
		}
		
		SimpleServiceLocator.buildCraftProxy.registerTrigger();
		
		ModuleItem = new ItemModule(Configs.ItemModuleId);
		ModuleItem.setItemName("itemModule");
		ModuleItem.loadModules();
		
		LogisticsItemDisk = new ItemDisk(Configs.ItemDiskId);
		LogisticsItemDisk.setItemName("itemDisk");
		LogisticsItemDisk.setIconIndex(3);
		
		SimpleServiceLocator.buildCraftProxy.registerPipes(event.getSide());
		
		ModLoader.addName(LogisticsNetworkMonitior, "Network monitor");
		if(DEBUG) ModLoader.addName(LogisticsItemCard, "Logistics Item Card");
		ModLoader.addName(LogisticsRemoteOrderer, "Remote Orderer");
		ModLoader.addName(LogisticsCraftingSignCreator, "Crafting Sign Creator");
		ModLoader.addName(ModuleItem, "BlankModule");
		ModLoader.addName(LogisticsItemDisk, "Logistics Disk");
		if(DEBUG) LanguageRegistry.instance().addNameForObject(LogisticsHUDArmor, "en_US", "Logistics HUD Glasses");
		if(DEBUG) LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsHUDParts,1,0), "en_US", "Logistics HUD Bow");
		if(DEBUG) LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsHUDParts,1,1), "en_US", "Logistics HUD Glass");
		if(DEBUG) LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsHUDParts,1,2), "en_US", "Logistics HUD Nose Bridge");
		
		RecipeManager.loadRecipes();
		
		SimpleServiceLocator.electricItemProxy.addCraftingRecipes();
		SimpleServiceLocator.forestryProxy.addCraftingRecipes();
		SimpleServiceLocator.addCraftingRecipeProvider(new AutoWorkbench());
		SimpleServiceLocator.addCraftingRecipeProvider(new SolderingStation());
		if (RollingMachine.load())
			SimpleServiceLocator.addCraftingRecipeProvider(new RollingMachine());
		
		if(DEBUG) SolderingStationRecipes.loadRecipe();
		
		//Blocks
		logisticsSign = new LogisticsSignBlock(Configs.LOGISTICS_SIGN_ID);
		ModLoader.registerBlock(logisticsSign);
		if(DEBUG) logisticsSolidBlock = new LogisticsSolidBlock(Configs.LOGISTICS_SOLID_BLOCK_ID);
		if(DEBUG) ModLoader.registerBlock(logisticsSolidBlock, LogisticsSolidBlockItem.class);
		MainProxy.proxy.registerTileEntitis();
	}
	
	@ServerStopping
	public void cleanup(FMLServerStoppingEvent event) {
		SimpleServiceLocator.routerManager.serverStopClean();
		ServerRouter.resetStatics();
	}
}
