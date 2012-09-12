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

import java.lang.reflect.Method;

import logisticspipes.blocks.LogisticsSignBlock;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.config.SolderingStationRecipes;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsManager;
import logisticspipes.items.CraftingSignCreator;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemModule;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.main.ActionDisableLogistics;
import logisticspipes.main.LogisticsItem;
import logisticspipes.main.LogisticsManager;
import logisticspipes.main.LogisticsTriggerProvider;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.main.TriggerSupplierFailed;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketHandler;
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.buildcraft.BuildCraftProxy3;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic2.ElectricItemProxy;
import logisticspipes.proxy.interfaces.IElectricItemProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.routing.RouterManager;
import logisticspipes.ticks.TickHandler;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.core.utils.Localization;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TransportProxyClient;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "LogisticsPipes|Main", name = "Logistics Pipes", version = "%VERSION%", useMetadata = true)
@NetworkMod(channels = {NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class LogisticsPipes {
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
	
	public static ItemModule ModuleItem;
	
	public static Trigger LogisticsFailedTrigger;
	
	public static Action LogisticsDisableAction;
	
	private Textures textures = new Textures();
	
	//Blocks
	Block logisticsSign;
	Block logisticsSolidBlock;
	
	@Deprecated
	public static ILogisticsManager logisticsManager = new LogisticsManager();
	
	/** Support for teleport pipes **/
	public static boolean teleportPipeDetected = false;
	public static Class<? extends Pipe> PipeItemTeleport;
	public static Method teleportPipeMethod;

	public LogisticsPipes() {
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy3());
		instance = this;
		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManagerV2());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
	}
	
	@Init
	public void init(FMLInitializationEvent event) {
		textures.load(event);
		if(event.getSide().isClient()) {
			Localization.addLocalization("/lang/logisticspipes/", "en_US");
		}
		NetworkRegistry.instance().registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		if(event.getSide().equals(Side.CLIENT) && DEBUG) {
			//WIP (highly alpha)
			TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
		}
	}
	
	@PreInit
	public void LoadConfig(FMLPreInitializationEvent evt) {
		Configs.load();
	}
	
	@PostInit
	public void PostLoad(FMLPostInitializationEvent event) {
		if(Loader.isModLoaded("mod_Forestry")) {
			SimpleServiceLocator.setForestryProxy(new ForestryProxy());
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
				@Override public String getNextAlleleId(String uid) {return null;}
				@Override public String getPrevAlleleId(String uid) {return null;}
			});
		}
		if(Loader.isModLoaded("mod_IC2")) {
			SimpleServiceLocator.setElectricItemProxy(new ElectricItemProxy());
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
		}

		try {
			PipeItemTeleport = (Class<? extends Pipe>) Class.forName("buildcraft.additionalpipes.pipes.PipeItemTeleport");
			//PipeItemTeleport = (Class<? extends Pipe>) Class.forName("net.minecraft.src.buildcraft.additionalpipes.pipes.PipeItemTeleport");
			teleportPipeMethod = PipeItemTeleport.getMethod("getConnectedPipes", boolean.class);
			teleportPipeDetected = true;
			ModLoader.getLogger().fine("Additional pipes detected, adding compatibility");

		} catch (Exception e) {
			try {
				//PipeItemTeleport = (Class<? extends Pipe>) Class.forName("buildcraft.additionalpipes.pipes.PipeItemTeleport");
				PipeItemTeleport = (Class<? extends Pipe>) Class.forName("net.minecraft.src.buildcraft.additionalpipes.pipes.PipeItemTeleport");
				teleportPipeMethod = PipeItemTeleport.getMethod("getConnectedPipes", boolean.class);
				teleportPipeDetected = true;
				ModLoader.getLogger().fine("Additional pipes detected, adding compatibility");

			} catch (Exception e1) {
				ModLoader.getLogger().fine("Additional pipes not detected: " + e1.getMessage());
			}
		}
				
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
		}
		
		LogisticsPipes.LogisticsFailedTrigger = new TriggerSupplierFailed(700);
		ActionManager.registerTriggerProvider(new LogisticsTriggerProvider());
		
		LogisticsPipes.LogisticsDisableAction = new ActionDisableLogistics(700);
		
		ModuleItem = new ItemModule(Configs.ItemModuleId);
		ModuleItem.setItemName("itemModule");
		ModuleItem.loadModules();
		
		LogisticsItemDisk = new ItemDisk(Configs.ItemDiskId);
		LogisticsItemDisk.setItemName("itemDisk");
		LogisticsItemDisk.setIconIndex(3);
		
		LogisticsBasicPipe = createPipe(Configs.LOGISTICSPIPE_BASIC_ID, PipeItemsBasicLogistics.class, "Basic Logistics Pipe", event.getSide());
		LogisticsRequestPipe = createPipe(Configs.LOGISTICSPIPE_REQUEST_ID, PipeItemsRequestLogistics.class, "Request Logistics Pipe", event.getSide());
		LogisticsProviderPipe = createPipe(Configs.LOGISTICSPIPE_PROVIDER_ID, PipeItemsProviderLogistics.class, "Provider Logistics Pipe", event.getSide());
		LogisticsCraftingPipe = createPipe(Configs.LOGISTICSPIPE_CRAFTING_ID, PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", event.getSide());
		LogisticsSatellitePipe = createPipe(Configs.LOGISTICSPIPE_SATELLITE_ID, PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", event.getSide());
		LogisticsSupplierPipe = createPipe(Configs.LOGISTICSPIPE_SUPPLIER_ID, PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", event.getSide());
		LogisticsChassiPipe1 = createPipe(Configs.LOGISTICSPIPE_CHASSI1_ID, PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", event.getSide());
		LogisticsChassiPipe2 = createPipe(Configs.LOGISTICSPIPE_CHASSI2_ID, PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", event.getSide());
		LogisticsChassiPipe3 = createPipe(Configs.LOGISTICSPIPE_CHASSI3_ID, PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", event.getSide());
		LogisticsChassiPipe4 = createPipe(Configs.LOGISTICSPIPE_CHASSI4_ID, PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", event.getSide());
		LogisticsChassiPipe5 = createPipe(Configs.LOGISTICSPIPE_CHASSI5_ID, PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", event.getSide());
		LogisticsCraftingPipeMK2 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK2_ID, PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", event.getSide());
		LogisticsRequestPipeMK2 = createPipe(Configs.LOGISTICSPIPE_REQUEST_MK2_ID, PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", event.getSide());
		LogisticsRemoteOrdererPipe = createPipe(Configs.LOGISTICSPIPE_REMOTE_ORDERER_ID, PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", event.getSide());
		LogisticsProviderPipeMK2 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_MK2_ID, PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", event.getSide());
		LogisticsApiaristAnalyserPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_ANALYSER_ID, PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", event.getSide());
		LogisticsApiaristSinkPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_SINK_ID, PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", event.getSide());
		if(DEBUG) LogisticsInvSysCon = createPipe(Configs.LOGISTICSPIPE_INVSYSCON_ID, PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", event.getSide());

		ModLoader.addName(LogisticsNetworkMonitior, "Network monitor");
		if(DEBUG) ModLoader.addName(LogisticsItemCard, "Logistics Item Card");
		ModLoader.addName(LogisticsRemoteOrderer, "Remote Orderer");
		ModLoader.addName(LogisticsCraftingSignCreator, "Crafting Sign Creator");
		ModLoader.addName(ModuleItem, "BlankModule");
		ModLoader.addName(LogisticsItemDisk, "Logistics Disk");
		if(DEBUG) ModLoader.addName(LogisticsHUDArmor, "Logistics HUD Glasses");
		
		/*
		LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE);
		*/
		
		LogisticsBuilderSupplierPipe = createPipe(Configs.LOGISTICSPIPE_BUILDERSUPPLIER_ID, PipeItemsBuilderSupplierLogistics.class, "Builder Supplier Logistics Pipe", event.getSide());
		LogisticsLiquidSupplierPipe = createPipe(Configs.LOGISTICSPIPE_LIQUIDSUPPLIER_ID, PipeItemsLiquidSupplier.class, "Liquid Supplier Logistics Pipe", event.getSide());
		
		CraftingManager craftingManager = CraftingManager.getInstance();
		craftingManager.addRecipe(new ItemStack(LogisticsBuilderSupplierPipe, 1), new Object[]{"iPy", Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('y'), new ItemStack(Item.dyePowder, 1,11)});
		//craftingManager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsLiquidSupplierPipe, 1), new Object[]{" B ", "lPl", " B ", Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('B'), Item.bucketEmpty});
		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		
		
		craftingManager.addRecipe(new ItemStack(LogisticsBasicPipe, 8), new Object[] { "grg", "GdG", "grg", Character.valueOf('g'), Block.glass, 
								   Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
								   Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond, 
								   Character.valueOf('r'), Block.torchRedstoneActive});
		craftingManager.addRecipe(new ItemStack(LogisticsBasicPipe, 8), new Object[] { "grg", "GdG", "grg", Character.valueOf('g'), Block.glass, 
								   Character.valueOf('G'), BuildCraftCore.goldGearItem,
								   Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond, 
								   Character.valueOf('r'), Block.torchRedstoneActive});

		craftingManager.addRecipe(new ItemStack(LogisticsProviderPipe, 1), new Object[] { "d", "P", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('d'), Item.lightStoneDust});
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipe, 1), new Object[] { "dPd", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('d'), Item.lightStoneDust});
		craftingManager.addRecipe(new ItemStack(LogisticsSatellitePipe, 1), new Object[] { "rPr", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('r'), Item.redstone});
		craftingManager.addRecipe(new ItemStack(LogisticsSupplierPipe, 1), new Object[] { "lPl", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4)});

		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipe, 1), new Object[] { "gPg", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('g'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipe, 1), new Object[] { "gPg", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('g'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsRequestPipe, Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsRequestPipe, Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsCraftingPipe, Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsCraftingPipe, Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrdererPipe, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsBasicPipe, Character.valueOf('U'), Item.enderPearl});
		
		craftingManager.addRecipe(new ItemStack(LogisticsItemDisk, 1), new Object[] { "igi", "grg", "igi", Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('r'), Item.redstone, Character.valueOf('g'), Item.goldNugget});
		
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.BLANK), new Object[] { "prp", "prp", "pgp", Character.valueOf('p'), Item.paper, Character.valueOf('r'), Item.redstone, Character.valueOf('g'), Item.goldNugget});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ITEMSINK), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 2),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ITEMSINK), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 2),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 1),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 1),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), Item.redstone});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
									Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
									Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
									Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 14),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 14),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.QUICKSORT), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.diamondGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.QUICKSORT), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.TERMINUS), new Object[] { "CGD", "rBr", "DrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 0),
									Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.TERMINUS), new Object[] { " G ", "rBr", "CrD", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 0),
									Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PROVIDER), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.goldGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PROVIDER), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		for(int i=0; i<1000;i++) {
			ILogisticsModule module = ((ItemModule)ModuleItem).getModuleForItem(new ItemStack(ModuleItem, 1, i), null, null, null, null);
			if(module != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				boolean force = false;
				try {
					module.writeToNBT(nbt, "");
				} catch(Exception e) {
					force = true;
				}
				if(!nbt.equals(new NBTTagCompound())) {
					registerShapelessResetRecipe(ModuleItem, i, ModuleItem, i);
				}
			}
		}
		
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe1, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.redstone});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe1, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 0)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe2, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.ingotIron});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe2, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe3, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.ingotGold});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe3, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe4, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.diamond});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe4, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe5, 1), new Object[] { "gig", "iPi", "gig", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Block.blockDiamond, Character.valueOf('g'), Block.blockGold});

		craftingManager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		
		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingSignCreator, 1), new Object[] {"G G", " S ", " D ", Character.valueOf('G'), BuildCraftCore.goldGearItem, Character.valueOf('S'), Item.sign, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingSignCreator, 1), new Object[] {"G G", " S ", " D ", Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2), Character.valueOf('S'), Item.sign, Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		SimpleServiceLocator.electricItemProxy.addCraftingRecipes();
		SimpleServiceLocator.forestryProxy.addCraftingRecipes();
		SimpleServiceLocator.addCraftingRecipeProvider(new AutoWorkbench());
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
	
	protected void registerShapelessResetRecipe(Item fromItem, int fromData, Item toItem, int toData) {
		for(int j=1;j < 10; j++) {
			Object[] obj = new Object[j];
			for(int k=0;k<j;k++) {
				obj[k] = new ItemStack(fromItem, 1, toData);
			}
			CraftingManager.getInstance().addShapelessRecipe(new ItemStack(toItem, j, fromData), obj);
		}
	}
	
	protected Item createPipe(int defaultID, Class <? extends Pipe> clas, String descr, Side side) {
		ItemPipe res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		
		if(side.isClient()) {
			LanguageRegistry.addName(res, descr);
			MinecraftForgeClient.registerItemRenderer(res.shiftedIndex, TransportProxyClient.pipeItemRenderer);
		}
		if(defaultID != Configs.LOGISTICSPIPE_BASIC_ID) {
			registerShapelessResetRecipe(res,0,LogisticsPipes.LogisticsBasicPipe,0);
		}
		return res;
	}
}
