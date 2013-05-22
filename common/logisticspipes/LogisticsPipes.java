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

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import logisticspipes.blocks.LogisticsSignBlock;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_IC2_BuildCraft;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.config.Configs;
import logisticspipes.items.CraftingSignCreator;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemParts;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsBrokenItem;
import logisticspipes.items.LogisticsItem;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.log.RequestLogFormator;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logistics.LogisticsLiquidManager;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.main.CreativeTabLP;
import logisticspipes.main.LogisticsEventListener;
import logisticspipes.main.LogisticsWorldManager;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.ProxyManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.SpecialInventoryHandlerManager;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.cc.LogisticsPowerJuntionTileEntity_CC_BuildCraft;
import logisticspipes.proxy.cc.LogisticsPowerJuntionTileEntity_CC_IC2_BuildCraft;
import logisticspipes.proxy.cc.LogisticsTileGenericPipe_CC;
import logisticspipes.proxy.recipeproviders.AssemblyAdvancedWorkbench;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.proxy.recipeproviders.SolderingStation;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.proxy.specialconnection.TeleportPipes;
import logisticspipes.proxy.specialconnection.TesseractConnection;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.renderer.LiquidContainerRenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.textures.Textures;
import logisticspipes.ticks.ClientPacketBufferHandlerThread;
import logisticspipes.ticks.HudUpdateTick;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.ticks.RenderTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.ServerPacketBufferHandlerThread;
import logisticspipes.ticks.WorldTickHandler;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.LiquidIdentifier;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.util.Icon;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.FingerprintWarning;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.Side;

@Mod(
		modid = "LogisticsPipes|Main",
		name = "Logistics Pipes",
		version = "%VERSION%",
		/* %------------CERTIFICATE-SUM-----------% */
		dependencies = "required-after:Forge@[6.5.0.0,);" +
				"required-after:BuildCraft|Core;" +
				"required-after:BuildCraft|Transport;" +
				"required-after:BuildCraft|Builders;" +
				"required-after:BuildCraft|Silicon;" +
				"after:IC2;" +
				"after:Forestry;" +
				"after:Thaumcraft;" +
				"after:CCTurtle;" +
				"after:ComputerCraft;" +
				"after:factorization;" +
				"after:GregTech_Addon;" +
				"after:AppliedEnergistics;" +
				"after:ThermalExpansion;" +
				"after:BetterStorage")
@NetworkMod(
		channels = {NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME},
		packetHandler = PacketHandler.class,
		clientSideRequired = true,
		serverSideRequired = true)
public class LogisticsPipes {

	@Instance("LogisticsPipes|Main")
	public static LogisticsPipes instance;

	//Log Requests
	public static boolean DisplayRequests;

	public static boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");
	public static boolean DEBUG_OVGEN = false;
	public static String MCVersion = "%MCVERSION%";
	
	private boolean certificateError = false;

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
	public static Item LogisticsEntrance;
	public static Item LogisticsDestination;
	public static Item LogisticsCraftingPipeMK3;
	public static Item LogisticsFirewall;
	
	//Liquid Pipes
	public static Item LogisticsLiquidConnector;
	public static Item LogisticsLiquidBasic;
	public static Item LogisticsLiquidInsertion;
	public static Item LogisticsLiquidProvider;
	public static Item LogisticsLiquidRequest;
	public static Item LogisticsLiquidExtractor;
	
	
	public static Item LogisticsNetworkMonitior;
	public static Item LogisticsRemoteOrderer;
	public static Item LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static Item LogisticsItemCard;
	public static ItemHUDArmor LogisticsHUDArmor;
	public static Item LogisticsParts;
	public static Item LogisticsUpgradeManager;
	public static Item LogisticsLiquidContainer;
	public static Item LogisticsBrokenItem;

	public static ItemModule ModuleItem;
	public static ItemUpgrade UpgradeItem;
	
	public static Textures textures = new Textures();
	
	public static Class<? extends LogisticsPowerJuntionTileEntity_BuildCraft> powerTileEntity;
	public static final String logisticsTileGenericPipeMapping = "logisticspipes.pipes.basic.LogisticsTileGenericPipe";
	
	public static CreativeTabLP LPCreativeTab = new CreativeTabLP();
	
	//Blocks
	public static Block logisticsSign;
	public static Block logisticsSolidBlock;
	
	public static Logger log;
	public static Logger requestLog;
	
	public static Icon teststuff;
	public static Icon teststuff2;
	@Init
	public void init(FMLInitializationEvent event) {
		
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy());
		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setSecurityStationManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManagerV2());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialPipeConnection());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialTileConnection());
		SimpleServiceLocator.setLogisticsLiquidManager(new LogisticsLiquidManager());
		
		if(event.getSide().isClient()) {
			SimpleServiceLocator.buildCraftProxy.registerLocalization();
		}
		NetworkRegistry.instance().registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		if(event.getSide().equals(Side.CLIENT)) {
			TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
		}
		if(!Configs.LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED) {
			TickRegistry.registerTickHandler(new WorldTickHandler(), Side.SERVER);
			TickRegistry.registerTickHandler(new WorldTickHandler(), Side.CLIENT);
		}
		TickRegistry.registerTickHandler(new QueuedTasks(), Side.SERVER);
		if(event.getSide() == Side.CLIENT) {
			SimpleServiceLocator.setClientPacketBufferHandlerThread(new ClientPacketBufferHandlerThread());
		}
		SimpleServiceLocator.setServerPacketBufferHandlerThread(new ServerPacketBufferHandlerThread());	
		for(int i=0; i<Configs.MULTI_THREAD_NUMBER; i++) {
			new RoutingTableUpdateThread(i);
		}
		MinecraftForge.EVENT_BUS.register(new LogisticsWorldManager());
		MinecraftForge.EVENT_BUS.register(new LogisticsEventListener());
		textures.registerBlockIcons();
		
		SimpleServiceLocator.buildCraftProxy.initProxyAndCheckVersion();
	}
	
	@PreInit
	public void LoadConfig(FMLPreInitializationEvent evt) {
		Configs.load(evt);
		log = evt.getModLog();
		requestLog = Logger.getLogger("LogisticsPipes|Request");
		requestLog.setUseParentHandlers(false);
		try {
			File logPath = new File((File) FMLInjectionData.data()[6], "LogisticsPipes-Request.log");
			FileHandler fileHandler = new FileHandler(logPath.getPath(), true);
			fileHandler.setFormatter(new RequestLogFormator());
			fileHandler.setLevel(Level.ALL);
			requestLog.addHandler(fileHandler);
		} catch (Exception e) {}
		if(DEBUG) {
			log.setLevel(Level.ALL);
		}
		if(certificateError) {
			log.severe("Certificate not correct");
			log.severe("This in not a LogisticsPipes version from RS485.");
		}
	}
	
	@SuppressWarnings("deprecation")
	@PostInit
	public void PostLoad(FMLPostInitializationEvent event) {
		
		boolean isClient = MainProxy.isClient();
		
		ProxyManager.load();
		SpecialInventoryHandlerManager.load();

		SimpleServiceLocator.specialpipeconnection.registerHandler(new TeleportPipes());
		SimpleServiceLocator.specialtileconnection.registerHandler(new TesseractConnection());
		
		LogisticsNetworkMonitior = new LogisticsItem(Configs.LOGISTICSNETWORKMONITOR_ID);
		LogisticsNetworkMonitior.setUnlocalizedName("networkMonitorItem");
		
		LogisticsItemCard = new LogisticsItemCard(Configs.ITEM_CARD_ID);
		LogisticsItemCard.setUnlocalizedName("logisticsItemCard");
		//LogisticsItemCard.setTabToDisplayOn(CreativeTabs.tabRedstone);
		
		LogisticsRemoteOrderer = new RemoteOrderer(Configs.LOGISTICSREMOTEORDERER_ID);
		LogisticsRemoteOrderer.setUnlocalizedName("remoteOrdererItem");

		LogisticsCraftingSignCreator = new CraftingSignCreator(Configs.LOGISTICSCRAFTINGSIGNCREATOR_ID);
		LogisticsCraftingSignCreator.setUnlocalizedName("CraftingSignCreator");
		
		int renderIndex;
		if(isClient) {
			renderIndex = RenderingRegistry.addNewArmourRendererPrefix("LogisticsHUD");
		} else {
			renderIndex = 0;
		}
		LogisticsHUDArmor = new ItemHUDArmor(Configs.ITEM_HUD_ID, renderIndex);
		LogisticsHUDArmor.setUnlocalizedName("logisticsHUDGlasses");
		
		LogisticsParts = new ItemParts(Configs.ITEM_PARTS_ID);
		LogisticsParts.setUnlocalizedName("logisticsParts");
		
		SimpleServiceLocator.buildCraftProxy.registerTrigger();
		
		ModuleItem = new ItemModule(Configs.ITEM_MODULE_ID);
		ModuleItem.setUnlocalizedName("itemModule");
		ModuleItem.loadModules();
		
		LogisticsItemDisk = new ItemDisk(Configs.ITEM_DISK_ID);
		LogisticsItemDisk.setUnlocalizedName("itemDisk");
		
		UpgradeItem = new ItemUpgrade(Configs.ITEM_UPGRADE_ID);
		UpgradeItem.setUnlocalizedName("itemUpgrade");
		UpgradeItem.loadUpgrades();
		
		//TODO make it visible in creative search
		LogisticsUpgradeManager = new LogisticsItem(Configs.ITEM_UPGRADE_MANAGER_ID);
		LogisticsUpgradeManager.setUnlocalizedName("upgradeManagerItem");
		
		if(DEBUG) {
			LogisticsLiquidContainer = new LogisticsLiquidContainer(Configs.ITEM_LIQUID_CONTAINER_ID);
			LogisticsLiquidContainer.setUnlocalizedName("logisticsLiquidContainer");
			if(isClient) {
				MinecraftForgeClient.registerItemRenderer(LogisticsLiquidContainer.itemID, new LiquidContainerRenderer());
			}
		}
		
		LogisticsBrokenItem = new LogisticsBrokenItem(Configs.ITEM_BROKEN_ID);
		LogisticsBrokenItem.setUnlocalizedName("brokenItem");
		
		SimpleServiceLocator.buildCraftProxy.registerPipes(event.getSide());
		
		ModLoader.addName(LogisticsNetworkMonitior, "Network monitor");
		ModLoader.addName(LogisticsItemCard, "Logistics Item Card");
		ModLoader.addName(LogisticsRemoteOrderer, "Remote Orderer");
		ModLoader.addName(LogisticsCraftingSignCreator, "Crafting Sign Creator");
		ModLoader.addName(ModuleItem, "BlankModule");
		ModLoader.addName(LogisticsItemDisk, "Logistics Disk");
		LanguageRegistry.instance().addNameForObject(LogisticsHUDArmor, "en_US", "Logistics HUD Glasses");
		LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsParts,1,0), "en_US", "Logistics HUD Bow");
		LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsParts,1,1), "en_US", "Logistics HUD Glass");
		LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsParts,1,2), "en_US", "Logistics HUD Nose Bridge");
		LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsParts,1,3), "en_US", "Nano Hopper");
		LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsUpgradeManager,1,0), "en_US", "Upgrade Manager");
		LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsBrokenItem,1,0), "en_US", "Logistics Broken Item");
		
		if(DEBUG) {
			LanguageRegistry.instance().addNameForObject(new ItemStack(LogisticsLiquidContainer,1,0), "en_US", "Logistics Liquid Container");
		}
		
		LanguageRegistry.instance().addStringLocalization("itemGroup.Logistics_Pipes", "en_US", "Logistics Pipes");
		
		SimpleServiceLocator.IC2Proxy.addCraftingRecipes();
		SimpleServiceLocator.forestryProxy.addCraftingRecipes();
		SimpleServiceLocator.thaumCraftProxy.addCraftingRecipes();
		SimpleServiceLocator.addCraftingRecipeProvider(new AutoWorkbench());
		SimpleServiceLocator.addCraftingRecipeProvider(new AssemblyAdvancedWorkbench());
		SimpleServiceLocator.addCraftingRecipeProvider(new SolderingStation());
		if (RollingMachine.load())
			SimpleServiceLocator.addCraftingRecipeProvider(new RollingMachine());
		
		SolderingStationRecipes.loadRecipe();
		
		//Blocks
		logisticsSign = new LogisticsSignBlock(Configs.LOGISTICS_SIGN_ID);
		ModLoader.registerBlock(logisticsSign);
		logisticsSign.setUnlocalizedName("logisticsSign");
		logisticsSolidBlock = new LogisticsSolidBlock(Configs.LOGISTICS_SOLID_BLOCK_ID);
		ModLoader.registerBlock(logisticsSolidBlock, LogisticsSolidBlockItem.class);
		logisticsSign.setUnlocalizedName("logisticsSolidBlock");
		//Power Junction
		if(SimpleServiceLocator.IC2Proxy.hasIC2()) {
			if(SimpleServiceLocator.ccProxy.isCC()) {
				powerTileEntity = LogisticsPowerJuntionTileEntity_CC_IC2_BuildCraft.class;
			} else {
				powerTileEntity = LogisticsPowerJuntionTileEntity_IC2_BuildCraft.class;
			}
		} else {
			if(SimpleServiceLocator.ccProxy.isCC()) {
				powerTileEntity = LogisticsPowerJuntionTileEntity_CC_BuildCraft.class;
			} else {
				powerTileEntity = LogisticsPowerJuntionTileEntity_BuildCraft.class;
			}
		}
		
		//LogisticsTileGenerticPipe
		if(SimpleServiceLocator.ccProxy.isCC()) {
			BuildCraftProxy.logisticsTileGenericPipe = LogisticsTileGenericPipe_CC.class;
		} else if(!Configs.LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED) {
			BuildCraftProxy.logisticsTileGenericPipe = LogisticsTileGenericPipe.class;
		}
		
		MainProxy.proxy.registerTileEntitis();

		RecipeManager.loadRecipes();
		
		//Registering special particles
		MainProxy.proxy.registerParticles();
		
		//init Liquids
		LiquidIdentifier.initFromForge(false);
		LiquidIdentifier.get(9, 0, "water");
		LiquidIdentifier.get(11, 0, "lava");
	}
	
	@ServerStopping
	public void cleanup(FMLServerStoppingEvent event) {
		SimpleServiceLocator.routerManager.serverStopClean();
		QueuedTasks.clearAllTasks();
		HudUpdateTick.clearUpdateFlags();
		BaseLogicSatellite.cleanup();
		ServerRouter.cleanup();
		if(event.getSide().equals(Side.CLIENT)) {
			LogisticsHUDRenderer.instance().clear();
		}
	}
	
	@ServerStarting
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new LogisticsPipesCommand());
	}
	
	@FingerprintWarning
	public void certificateWarning(FMLFingerprintViolationEvent warning) {
		if(!DEBUG) {
			System.out.println("[LogisticsPipes|Certificate] Certificate not correct");
			System.out.println("[LogisticsPipes|Certificate] Expected: " + warning.expectedFingerprint);
			System.out.println("[LogisticsPipes|Certificate] File: " + warning.source.getAbsolutePath());
			System.out.println("[LogisticsPipes|Certificate] This in not a LogisticsPipes version from RS485.");
			certificateError = true;
		}
	}
}
