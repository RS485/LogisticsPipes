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
import logisticspipes.items.LogisticsItem;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.log.RequestLogFormator;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.main.CreativeTabLP;
import logisticspipes.main.LogisticsWorldManager;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.cc.CCProxy;
import logisticspipes.proxy.cc.CCTurtleProxy;
import logisticspipes.proxy.cc.LogisticsPowerJuntionTileEntity_CC_BuildCraft;
import logisticspipes.proxy.cc.LogisticsPowerJuntionTileEntity_CC_IC2_BuildCraft;
import logisticspipes.proxy.cc.LogisticsTileGenericPipe_CC;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic2.ElectricItemProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.IElectricItemProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import logisticspipes.proxy.recipeproviders.AssemblyAdvancedWorkbench;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.proxy.recipeproviders.SolderingStation;
import logisticspipes.proxy.specialconnection.SpecialConnection;
import logisticspipes.proxy.specialconnection.TeleportPipes;
import logisticspipes.proxy.specialinventoryhandler.BarrelInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.CrateInventoryHandler;
import logisticspipes.proxy.specialinventoryhandler.QuantumChestHandler;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.textures.Textures;
import logisticspipes.ticks.ClientPacketBufferHandlerThread;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.ticks.RenderTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.ServerPacketBufferHandlerThread;
import logisticspipes.ticks.WorldTickHandler;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
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
import logisticspipes.proxy.thaumcraft.ThaumCraftProxy;

@Mod(modid = "LogisticsPipes|Main", name = "Logistics Pipes", version = "%VERSION%", dependencies = "required-after:BuildCraft|Transport;required-after:BuildCraft|Builders;required-after:BuildCraft|Silicon;after:IC2;after:Forestry;after:Thaumcraft;after:CCTurtle;after:ComputerCraft;after:factorization;after:GregTech_Addon;after:BetterStorage", useMetadata = true)
@NetworkMod(channels = {NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class LogisticsPipes {
	

	@Instance("LogisticsPipes|Main")
	public static LogisticsPipes instance;

	//Log Requests
	public static boolean DisplayRequests;

	public static boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");
	public static String MCVersion = "%MCVERSION%";

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
	
	
	public static Item LogisticsNetworkMonitior;
	public static Item LogisticsRemoteOrderer;
	public static Item LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static Item LogisticsItemCard;
	public static ItemHUDArmor LogisticsHUDArmor;
	public static Item LogisticsParts;
	public static Item LogisticsUpgradeManager;

	public static ItemModule ModuleItem;
	public static ItemUpgrade UpgradeItem;
	
	private Textures textures = new Textures();
	
	public static Class<? extends LogisticsPowerJuntionTileEntity_BuildCraft> powerTileEntity;	
	public static Class<? extends TileGenericPipe> logisticsTileGenericPipe;
	public static final String logisticsTileGenericPipeMapping = "logisticspipes.pipes.basic.LogisticsTileGenericPipe";
	
	public static CreativeTabLP LPCreativeTab = new CreativeTabLP();
	
	//Blocks
	public static Block logisticsSign;
	public static Block logisticsSolidBlock;
	
	public static Logger log;
	public static Logger requestLog;
	
	@Init
	public void init(FMLInitializationEvent event) {
		
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy());
		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManagerV2());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialConnection());
		
		textures.load(event);
		
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
			SimpleServiceLocator.setServerPacketBufferHandlerThread(new ServerPacketBufferHandlerThread());
		} else {
			SimpleServiceLocator.setServerPacketBufferHandlerThread(new ServerPacketBufferHandlerThread());	
		}
		for(int i=0;i<Configs.multiThreadNumber && Configs.multiThreadEnabled;i++) {
			new RoutingTableUpdateThread(i);
		}
		MinecraftForge.EVENT_BUS.register(new LogisticsWorldManager());
	}
	
	@PreInit
	public void LoadConfig(FMLPreInitializationEvent evt) {
		Configs.load();
		log = evt.getModLog();
		requestLog = Logger.getLogger("LogisticsPipes|Request");
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
	}
	
	@PostInit
	public void PostLoad(FMLPostInitializationEvent event) {
		if(Loader.isModLoaded("Forestry")) {
			SimpleServiceLocator.setForestryProxy(new ForestryProxy());
			log.info("Loaded ForestryProxy");
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
				@Override public String getNextAlleleId(String uid, World world) {return "";}
				@Override public String getPrevAlleleId(String uid, World world) {return "";}
			});
			log.info("Loaded Forestry DummyProxy");
		}
		if(Loader.isModLoaded("IC2")) {
			SimpleServiceLocator.setElectricItemProxy(new ElectricItemProxy());
			log.info("Loaded IC2Proxy");
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
				@Override public boolean hasIC2() {return false;}
			});
			log.info("Loaded IC2 DummyProxy");
		}
		if(Loader.isModLoaded("ComputerCraft")) {
			if(Loader.isModLoaded("CCTurtle")) {
				SimpleServiceLocator.setCCProxy(new CCTurtleProxy());
				log.info("Loaded CCTurtleProxy");
			} else {
				SimpleServiceLocator.setCCProxy(new CCProxy());
				log.info("Loaded CCProxy");
			}
		} else {
			//DummyProxy
			SimpleServiceLocator.setCCProxy(new ICCProxy() {
				@Override public boolean isTurtle(TileEntity tile) {return false;}
				@Override public boolean isComputer(TileEntity tile) {return false;}
				@Override public boolean isCC() {return false;}
				@Override public ForgeDirection getOrientation(Object computer, int side, TileEntity tile) {return ForgeDirection.UNKNOWN;}
				@Override public boolean isLuaThread(Thread thread) {return false;}
			});
			log.info("Loaded CC DummyProxy");
		}
		
		if(Loader.isModLoaded("Thaumcraft")) {
			SimpleServiceLocator.setThaumCraftProxy(new ThaumCraftProxy());
			log.info("Loaded Thaumcraft Proxy");
		} else {
			SimpleServiceLocator.setThaumCraftProxy(new IThaumCraftProxy() {
				@Override public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {}
			});
			log.info("Loaded Thaumcraft DummyProxy");
		}
		
		if(Loader.isModLoaded("factorization")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BarrelInventoryHandler());
		}
		
		if(Loader.isModLoaded("GregTech_Addon")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new QuantumChestHandler());
		}

		if(Loader.isModLoaded("BetterStorage")) {
			SimpleServiceLocator.inventoryUtilFactory.registerHandler(new CrateInventoryHandler());
		}

		SimpleServiceLocator.specialconnection.registerHandler(new TeleportPipes());
		
		LogisticsNetworkMonitior = new LogisticsItem(Configs.LOGISTICSNETWORKMONITOR_ID);
		LogisticsNetworkMonitior.setIconIndex(Textures.LOGISTICSNETWORKMONITOR_ICONINDEX);
		LogisticsNetworkMonitior.setItemName("networkMonitorItem");
		
		LogisticsItemCard = new LogisticsItemCard(Configs.ItemCardId);
		LogisticsItemCard.setIconIndex(Textures.LOGISTICSITEMCARD_ICONINDEX);
		LogisticsItemCard.setItemName("logisticsItemCard");
		//LogisticsItemCard.setTabToDisplayOn(CreativeTabs.tabRedstone);
		
		LogisticsRemoteOrderer = new RemoteOrderer(Configs.LOGISTICSREMOTEORDERER_ID);
		//LogisticsRemoteOrderer.setIconIndex(LOGISTICSREMOTEORDERER_ICONINDEX);
		LogisticsRemoteOrderer.setItemName("remoteOrdererItem");

		LogisticsCraftingSignCreator = new CraftingSignCreator(Configs.LOGISTICSCRAFTINGSIGNCREATOR_ID);
		LogisticsCraftingSignCreator.setIconIndex(Textures.LOGISTICSCRAFTINGSIGNCREATOR_ICONINDEX);
		LogisticsCraftingSignCreator.setItemName("CraftingSignCreator");
		
		int renderIndex;
		if(MainProxy.isClient()) {
			renderIndex = RenderingRegistry.addNewArmourRendererPrefix("LogisticsHUD");
		} else {
			renderIndex = 0;
		}
		LogisticsHUDArmor = new ItemHUDArmor(Configs.ItemHUDId, renderIndex);
		LogisticsHUDArmor.setIconIndex(Textures.LOGISTICSITEMHUD_ICONINDEX);
		LogisticsHUDArmor.setItemName("logisticsHUDGlasses");
		
		LogisticsParts = new ItemParts(Configs.ItemPartsId);
		LogisticsParts.setIconIndex(Textures.LOGISTICSITEMHUD_PART3_ICONINDEX);
		LogisticsParts.setItemName("logisticsParts");
		
		SimpleServiceLocator.buildCraftProxy.registerTrigger();
		
		ModuleItem = new ItemModule(Configs.ItemModuleId);
		ModuleItem.setItemName("itemModule");
		ModuleItem.loadModules();
		
		LogisticsItemDisk = new ItemDisk(Configs.ItemDiskId);
		LogisticsItemDisk.setItemName("itemDisk");
		LogisticsItemDisk.setIconIndex(3);
		
		UpgradeItem = new ItemUpgrade(Configs.ItemUpgradeId);
		UpgradeItem.setItemName("itemUpgrade");
		UpgradeItem.loadUpgrades();
		
		LogisticsUpgradeManager = new LogisticsItem(Configs.ItemUpgradeManagerId);
		LogisticsUpgradeManager.setIconIndex(Textures.LOGISTICSITEM_UPGRADEMANAGER_ICONINDEX);
		LogisticsUpgradeManager.setItemName("upgradeManagerItem");
		
		
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
		
		SimpleServiceLocator.electricItemProxy.addCraftingRecipes();
		SimpleServiceLocator.forestryProxy.addCraftingRecipes();
		SimpleServiceLocator.addCraftingRecipeProvider(new AutoWorkbench());
		SimpleServiceLocator.addCraftingRecipeProvider(new AssemblyAdvancedWorkbench());
		SimpleServiceLocator.addCraftingRecipeProvider(new SolderingStation());
		if (RollingMachine.load())
			SimpleServiceLocator.addCraftingRecipeProvider(new RollingMachine());
		
		SolderingStationRecipes.loadRecipe();
		
		//Blocks
		logisticsSign = new LogisticsSignBlock(Configs.LOGISTICS_SIGN_ID);
		ModLoader.registerBlock(logisticsSign);
		logisticsSolidBlock = new LogisticsSolidBlock(Configs.LOGISTICS_SOLID_BLOCK_ID);
		ModLoader.registerBlock(logisticsSolidBlock, LogisticsSolidBlockItem.class);
		
		//Power Junction
		if(SimpleServiceLocator.electricItemProxy.hasIC2()) {
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
			logisticsTileGenericPipe = LogisticsTileGenericPipe_CC.class;
		} else {
			logisticsTileGenericPipe = LogisticsTileGenericPipe.class;
		}
		
		MainProxy.proxy.registerTileEntitis();

		RecipeManager.loadRecipes();
		
		//Registering special particles
		MainProxy.proxy.registerParticles();
	}
	
	@ServerStopping
	public void cleanup(FMLServerStoppingEvent event) {
		SimpleServiceLocator.routerManager.serverStopClean();
		ServerRouter.resetStatics();
		if(event.getSide().equals(Side.CLIENT)) {
			LogisticsHUDRenderer.instance().clear();
		}
	}
	
	@ServerStarting
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new LogisticsPipesCommand());
	}
}
