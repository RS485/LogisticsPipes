/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.GameData;

import org.apache.logging.log4j.Logger;

import logisticspipes.asm.LogisticsPipesClassInjector;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.config.Configs;
import logisticspipes.config.PlayerConfig;
import logisticspipes.items.ItemBlankModule;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemLogisticsChips;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.items.ItemLogisticsProgrammer;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemParts;
import logisticspipes.items.ItemPipeController;
import logisticspipes.items.ItemPipeManager;
import logisticspipes.items.ItemPipeSignCreator;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsBrokenItem;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.items.LogisticsItem;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logistics.LogisticsFluidManager;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeFluidBasic;
import logisticspipes.pipes.PipeFluidExtractor;
import logisticspipes.pipes.PipeFluidInsertion;
import logisticspipes.pipes.PipeFluidProvider;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericSubMultiBlock;
import logisticspipes.pipes.tubes.HSTubeCurve;
import logisticspipes.pipes.tubes.HSTubeGain;
import logisticspipes.pipes.tubes.HSTubeLine;
import logisticspipes.pipes.tubes.HSTubeSCurve;
import logisticspipes.pipes.tubes.HSTubeSpeedup;
import logisticspipes.pipes.unrouted.PipeItemsBasicTransport;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.ProxyManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.SpecialInventoryHandlerManager;
import logisticspipes.proxy.SpecialTankHandlerManager;
import logisticspipes.proxy.computers.objects.LPGlobalCCAccess;
import logisticspipes.proxy.endercore.EnderCoreProgressProvider;
import logisticspipes.proxy.forestry.ForestryProgressProvider;
import logisticspipes.proxy.ic2.IC2ProgressProvider;
import logisticspipes.proxy.progressprovider.MachineProgressProvider;
import logisticspipes.proxy.recipeproviders.ImmibisCraftingTableMk2;
import logisticspipes.proxy.recipeproviders.LogisticsCraftingTable;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.proxy.recipeproviders.SolderingStation;
import logisticspipes.proxy.specialconnection.EnderIOTransceiverConnection;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.proxy.specialtankhandler.SpecialTankHandler;
import logisticspipes.proxy.te.ThermalExpansionProgressProvider;
import logisticspipes.recipes.BlockChippedCraftingRecipes;
import logisticspipes.recipes.ChippedCraftingRecipes;
import logisticspipes.recipes.CraftingPartRecipes;
import logisticspipes.recipes.CraftingParts;
import logisticspipes.recipes.LPChipRecipes;
import logisticspipes.recipes.ModuleChippedCraftingRecipes;
import logisticspipes.recipes.PipeChippedCraftingRecipes;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.CraftingRecipes;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.renderer.newpipe.LogisticsBlockModel;
import logisticspipes.renderer.newpipe.LogisticsNewPipeModel;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.textures.Textures;
import logisticspipes.ticks.ClientPacketBufferHandlerThread;
import logisticspipes.ticks.HudUpdateTick;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.ticks.RenderTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.ServerPacketBufferHandlerThread;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.TankUtilFactory;

//@formatter:off
//CHECKSTYLE:OFF

@Mod(
		modid = LPConstants.LP_MOD_ID,
		name = "Logistics Pipes",
		version = "%VERSION%",
		/* %------------CERTIFICATE-SUM-----------% */
		dependencies = "required-after:forge@[10.12.1.1079,);" +
				"after:buildcraft|Core;" +
				"after:buildcraft|Transport;" +
				"after:buildcraft|Silicon;" +
				"after:buildcraft|Robotics;" +
				"after:ic2;" +
				"after:forestry;" +
				"after:computercraft;" +
				"after:factorization;" +
				"after:gregtech_addon;" +
				"after:appliedenergistics;" +
				"after:thermalexpansion;" +
		"after:BetterStorage")
public class LogisticsPipes {

	//@formatter:on
	//CHECKSTYLE:ON

	public LogisticsPipes() { //TODO: remove throws
		LaunchClassLoader loader = Launch.classLoader;
		if (!LPConstants.COREMOD_LOADED) {
			if (LPConstants.DEBUG) {
				throw new RuntimeException("LogisticsPipes FMLLoadingPlugin wasn't loaded. If you are running MC from an IDE make sure to copy the 'LogisticsPipes_dummy.jar' to your mods folder. If you are running MC normal please report this as a bug at 'https://github.com/RS485/LogisticsPipes/issues'.");
			} else {
				throw new RuntimeException("LogisticsPipes FMLLoadingPlugin wasn't loaded. Your download seems to be corrupt/modified. Please redownload LP from our Jenkins [http://ci.rs485.network] and move it into your mods folder.");
			}
		}
		try {
			Field fTransformers = LaunchClassLoader.class.getDeclaredField("transformers");
			fTransformers.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IClassTransformer> transformers = (List<IClassTransformer>) fTransformers.get(loader);
			IClassTransformer lpClassInjector = new LogisticsPipesClassInjector();
			transformers.add(lpClassInjector);
			// Avoid NPE caused by wrong ClassTransformers
			for (int i = transformers.size() - 1; i > 0; i--) { // Move everything one up
				transformers.set(i, transformers.get(i - 1));
			}
			transformers.set(0, lpClassInjector); // So that our injector can be first
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			loader.registerTransformer("logisticspipes.asm.LogisticsPipesClassInjector");
			e.printStackTrace();
		}
		PacketHandler.initialize();
		NewGuiHandler.initialize();

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.Instance("logisticspipes")
	public static LogisticsPipes instance;

	//Log Requests
	public static boolean DisplayRequests;

	public static boolean WATCHDOG = false;

	private boolean certificateError = false;

	// Logistics Pipes
	public static Item LogisticsBasicPipe;
	public static Item LogisticsRequestPipeMk1;
	public static Item LogisticsRequestPipeMk2;
	public static Item LogisticsProviderPipeMk1;
	public static Item LogisticsProviderPipeMk2;
	public static Item LogisticsCraftingPipeMk1;
	public static Item LogisticsCraftingPipeMk2;
	public static Item LogisticsCraftingPipeMk3;
	public static Item LogisticsSatellitePipe;
	public static Item LogisticsSupplierPipe;
	public static Item LogisticsChassisPipeMk1;
	public static Item LogisticsChassisPipeMk2;
	public static Item LogisticsChassisPipeMk3;
	public static Item LogisticsChassisPipeMk4;
	public static Item LogisticsChassisPipeMk5;
	public static Item LogisticsRemoteOrdererPipe;
	public static Item LogisticsInvSysConPipe;
	public static Item LogisticsEntrancePipe;
	public static Item LogisticsDestinationPipe;
	public static Item LogisticsFirewallPipe;
	public static Item logisticsRequestTable;

	// Logistics Apiarist's Pipes
	public static Item LogisticsApiaristAnalyzerPipe;
	public static Item LogisticsApiaristSinkPipe;

	// Logistics Fluid Pipes
	public static Item LogisticsFluidBasicPipe;
	public static Item LogisticsFluidRequestPipe;
	public static Item LogisticsFluidProviderPipe;
	public static Item LogisticsFluidSatellitePipe;
	public static Item LogisticsFluidSupplierPipeMk1;
	public static Item LogisticsFluidSupplierPipeMk2;
	public static Item LogisticsFluidInsertionPipe;
	public static Item LogisticsFluidExtractorPipe;

	//Transport Pipes
	public static Item BasicTransportPipe;

	//Tubes
	public static Item HSTubeCurve;
	public static Item HSTubeSpeedup;
	public static Item HSTubeSCurve;
	public static Item HSTubeLine;
	public static Item HSTubeGain;

	// Logistics Modules/Upgrades
	public static ItemBlankModule LogisticsBlankModule;
	public static Map<Class<? extends LogisticsModule>, ItemModule> LogisticsModules = new HashMap<>();
	//public static ItemModule ModuleItem;
	public static ItemUpgrade UpgradeItem;

	// Miscellaneous Items
	public static RemoteOrderer LogisticsRemoteOrderer;
	public static ItemPipeSignCreator LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static LogisticsItemCard LogisticsItemCard;
	public static ItemHUDArmor LogisticsHUDArmor;
	public static ItemParts LogisticsParts;
	public static LogisticsFluidContainer LogisticsFluidContainer;
	public static LogisticsBrokenItem LogisticsBrokenItem;
	public static ItemPipeController LogisticsPipeControllerItem;
	public static ItemPipeManager LogisticsPipeManagerItem;
	public static ItemLogisticsProgrammer LogisticsProgrammer;

	public static ItemLogisticsChips LogisticsChips_basic;
	public static ItemLogisticsChips LogisticsChips_basic_raw;
	public static ItemLogisticsChips LogisticsChips_advanced;
	public static ItemLogisticsChips LogisticsChips_advanced_raw;
	public static ItemLogisticsChips LogisticsChips_fpga;
	public static ItemLogisticsChips LogisticsChips_fpga_raw;

	// Logistics Blocks
	public static LogisticsSolidBlock LogisticsSolidBlock;
	public static LogisticsBlockGenericPipe LogisticsPipeBlock;
	public static LogisticsBlockGenericSubMultiBlock LogisticsSubMultiBlock;

	// other statics
	public static Textures textures = new Textures();
	public static final String logisticsTileGenericPipeMapping = "logisticspipes.pipes.basic.LogisticsTileGenericPipe";
	public static CreativeTabLP LPCreativeTab = new CreativeTabLP();
	public static Logger log;
	public static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public static VersionChecker versionChecker;

	private Queue<Runnable> postInitRun = new LinkedList<>();
	private static LPGlobalCCAccess generalAccess;
	private static PlayerConfig playerConfig;

	@CapabilityInject(IItemHandler.class)
	public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;

	@CapabilityInject(IFluidHandler.class)
	public static Capability<IFluidHandler> FLUID_HANDLER_CAPABILITY = null;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {

		//Register Network channels
		MainProxy.createChannels();

		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setSecurityStationManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManager());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		SimpleServiceLocator.setTankUtilFactory(new TankUtilFactory());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialPipeConnection());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialTileConnection());
		SimpleServiceLocator.setSpecialTankHandler(new SpecialTankHandler());
		SimpleServiceLocator.setMachineProgressProvider(new MachineProgressProvider());
		SimpleServiceLocator.setRoutedItemHelper(new RoutedItemHelper());

		NetworkRegistry.INSTANCE.registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		FMLCommonHandler.instance().bus().register(new LPTickHandler());

		if (event.getSide().equals(Side.CLIENT)) {
			RenderTickHandler sub = new RenderTickHandler();
			FMLCommonHandler.instance().bus().register(sub);
			MinecraftForge.EVENT_BUS.register(sub);
		}
		FMLCommonHandler.instance().bus().register(new QueuedTasks());
		if (event.getSide() == Side.CLIENT) {
			SimpleServiceLocator.setClientPacketBufferHandlerThread(new ClientPacketBufferHandlerThread());
		}
		SimpleServiceLocator.setServerPacketBufferHandlerThread(new ServerPacketBufferHandlerThread());
		for (int i = 0; i < Configs.MULTI_THREAD_NUMBER; i++) {
			new RoutingTableUpdateThread(i);
		}
		LogisticsEventListener eventListener = new LogisticsEventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);
		FMLCommonHandler.instance().bus().register(eventListener);
		MinecraftForge.EVENT_BUS.register(new LPChatListener());

		RecipeManager.registerRecipeClasses();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		LogisticsPipes.log = evt.getModLog();
		loadClasses();
		ProxyManager.load();
		Configs.load();
		if (certificateError) {
			LogisticsPipes.log.fatal("Certificate not correct");
			LogisticsPipes.log.fatal("This in not a LogisticsPipes version from RS485.");
		}
		if (LPConstants.DEV_BUILD) {
			LogisticsPipes.log.debug("You are using a dev version.");
			LogisticsPipes.log.debug("While the dev versions contain cutting edge features, they may also contain more bugs.");
			LogisticsPipes.log.debug("Please report any you find to https://github.com/RS485/LogisticsPipes/issues");
		}

		SimpleServiceLocator.setPipeInformationManager(new PipeInformationManager());
		SimpleServiceLocator.setLogisticsFluidManager(new LogisticsFluidManager());

		ModelLoaderRegistry.registerLoader(new LogisticsNewPipeModel.LogisticsNewPipeModelLoader());
		ModelLoaderRegistry.registerLoader(new LogisticsBlockModel.LogisticsBlockModelLoader());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		postInitRun.forEach(Runnable::run);
		postInitRun = null;

		SpecialInventoryHandlerManager.load();
		SpecialTankHandlerManager.load();

		SimpleServiceLocator.buildCraftProxy.registerPipeInformationProvider();
		SimpleServiceLocator.buildCraftProxy.initProxy();
		SimpleServiceLocator.buildCraftProxy.registerTrigger();

		SimpleServiceLocator.thermalDynamicsProxy.registerPipeInformationProvider();

		//SimpleServiceLocator.specialpipeconnection.registerHandler(new TeleportPipes());
		//SimpleServiceLocator.specialtileconnection.registerHandler(new TesseractConnection());
		//SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOHyperCubeConnection());
		SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOTransceiverConnection());

		//SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Factory", "AutoWorkbench", AutoWorkbench.class));
		//SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Silicon", "AssemblyAdvancedWorkbench", AssemblyAdvancedWorkbench.class));
		if (SimpleServiceLocator.buildCraftProxy.getAssemblyTableProviderClass() != null) {
			SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Silicon", "AssemblyTable", SimpleServiceLocator.buildCraftProxy.getAssemblyTableProviderClass()));
		}
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("Railcraft", "RollingMachine", RollingMachine.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("Tubestuff", "ImmibisCraftingTableMk2", ImmibisCraftingTableMk2.class));
		SimpleServiceLocator.addCraftingRecipeProvider(new SolderingStation());
		SimpleServiceLocator.addCraftingRecipeProvider(new LogisticsCraftingTable());

		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("Forestry", "Generic", ForestryProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("ThermalExpansion", "Generic", ThermalExpansionProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("IC2", "Generic", IC2ProgressProvider.class));
		//SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("EnderIO", "Generic", EnderIOProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("endercore", "Generic", EnderCoreProgressProvider.class));

		MainProxy.proxy.registerTileEntities();

		//Registering special particles
		MainProxy.proxy.registerParticles();

		//init Fluids
		FluidIdentifier.initFromForge(false);

		versionChecker = VersionChecker.runVersionCheck();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureLoad(TextureStitchEvent.Pre event) {
		if (!event.getMap().getBasePath().equals("textures")) {
			return;
		}
		MainProxy.proxy.registerTextures();
	}

	@SubscribeEvent
	public void initItems(RegistryEvent.Register<Item> event) {

		//boolean isClient = event.get == Side.CLIENT;

		LogisticsPipes.LogisticsItemCard = new LogisticsItemCard();
		LogisticsPipes.LogisticsItemCard.setUnlocalizedName("logisticsItemCard");
		LogisticsPipes.LogisticsItemCard.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticsitemcard"));
		registerItem(LogisticsPipes.LogisticsItemCard);

		LogisticsPipes.LogisticsRemoteOrderer = new RemoteOrderer();
		LogisticsPipes.LogisticsRemoteOrderer.setUnlocalizedName("remoteOrdererItem");
		LogisticsPipes.LogisticsRemoteOrderer.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "remoteordereritem"));
		registerItem(LogisticsPipes.LogisticsRemoteOrderer);

		ItemPipeSignCreator.registerPipeSignTypes();
		LogisticsPipes.LogisticsCraftingSignCreator = new ItemPipeSignCreator();
		LogisticsPipes.LogisticsCraftingSignCreator.setUnlocalizedName("ItemPipeSignCreator");
		LogisticsPipes.LogisticsCraftingSignCreator.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "itempipesigncreator"));
		registerItem(LogisticsPipes.LogisticsCraftingSignCreator);

		LogisticsPipes.LogisticsHUDArmor = new ItemHUDArmor();
		LogisticsPipes.LogisticsHUDArmor.setUnlocalizedName("logisticsHUDGlasses");
		LogisticsPipes.LogisticsHUDArmor.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticshudglasses"));
		event.getRegistry().register(LogisticsPipes.LogisticsHUDArmor);

		LogisticsPipes.LogisticsParts = registerItem(new ItemParts());

		LogisticsPipes.LogisticsBlankModule = registerItem(new ItemBlankModule());
		ItemModule.loadModules();

		LogisticsPipes.LogisticsItemDisk = new ItemDisk();
		LogisticsPipes.LogisticsItemDisk.setUnlocalizedName("itemDisk");
		LogisticsPipes.LogisticsItemDisk.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "itemdisk"));
		registerItem(LogisticsPipes.LogisticsItemDisk);

		LogisticsPipes.UpgradeItem = new ItemUpgrade();
		LogisticsPipes.UpgradeItem.setUnlocalizedName("itemUpgrade");
		LogisticsPipes.UpgradeItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "itemupgrade"));
		LogisticsPipes.UpgradeItem.loadUpgrades();
		registerItem(LogisticsPipes.UpgradeItem);

		LogisticsPipes.LogisticsFluidContainer = new LogisticsFluidContainer();
		LogisticsPipes.LogisticsFluidContainer.setUnlocalizedName("logisticsFluidContainer");
		LogisticsPipes.LogisticsFluidContainer.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticsfluidcontainer"));
		event.getRegistry().register(LogisticsPipes.LogisticsFluidContainer);

		LogisticsPipes.LogisticsBrokenItem = new LogisticsBrokenItem();
		LogisticsPipes.LogisticsBrokenItem.setUnlocalizedName("brokenItem");
		LogisticsPipes.LogisticsBrokenItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "brokenitem"));
		registerItem(LogisticsPipes.LogisticsBrokenItem);

		LogisticsPipes.LogisticsPipeControllerItem = new ItemPipeController();
		LogisticsPipes.LogisticsPipeControllerItem.setUnlocalizedName("pipeController");
		LogisticsPipes.LogisticsPipeControllerItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "pipecontroller"));
		registerItem(LogisticsPipes.LogisticsPipeControllerItem);

		LogisticsPipes.LogisticsPipeManagerItem = new ItemPipeManager();
		LogisticsPipes.LogisticsPipeManagerItem.setUnlocalizedName("pipeManager");
		LogisticsPipes.LogisticsPipeManagerItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "pipemanager"));
		registerItem(LogisticsPipes.LogisticsPipeManagerItem);

		LogisticsPipes.LogisticsProgrammer = new ItemLogisticsProgrammer();
		LogisticsPipes.LogisticsProgrammer.setUnlocalizedName("logisticsProgrammer");
		LogisticsPipes.LogisticsProgrammer.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticsprogrammer"));
		registerItem(LogisticsPipes.LogisticsProgrammer);

		LogisticsChips_basic = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC));
		LogisticsChips_basic_raw = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC_RAW));
		LogisticsChips_advanced = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED));
		LogisticsChips_advanced_raw = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED_RAW));
		LogisticsChips_fpga = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA));
		LogisticsChips_fpga_raw = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA_RAW));

		registerPipes();

		registerRecipes();


		event.getRegistry().register(new LogisticsSolidBlockItem(LogisticsPipes.LogisticsSolidBlock).registerModels().setRegistryName(LogisticsPipes.LogisticsSolidBlock.getRegistryName()));
		//event.getRegistry().register(new ItemBlock(LogisticsPipes.LogisticsPipeBlock).setRegistryName(LogisticsPipes.LogisticsPipeBlock.getRegistryName()));
		//event.getRegistry().register(new ItemBlock(LogisticsPipes.LogisticsSubMultiBlock).setRegistryName(LogisticsPipes.LogisticsSubMultiBlock.getRegistryName()));
	}

	@SubscribeEvent
	public void initBlocks(RegistryEvent.Register<Block> event) {
		//Blocks
		LogisticsPipes.LogisticsSolidBlock = new LogisticsSolidBlock();
		LogisticsPipes.LogisticsSolidBlock.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, LogisticsPipes.LogisticsSolidBlock.getUnlocalizedName()));
		event.getRegistry().register(LogisticsPipes.LogisticsSolidBlock);


		LogisticsPipes.LogisticsPipeBlock = new LogisticsBlockGenericPipe();
		LogisticsPipes.LogisticsPipeBlock.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, LogisticsPipes.LogisticsPipeBlock.getUnlocalizedName()));
		event.getRegistry().register(LogisticsPipes.LogisticsPipeBlock);

		LogisticsPipes.LogisticsSubMultiBlock = new LogisticsBlockGenericSubMultiBlock();
		LogisticsPipes.LogisticsSubMultiBlock.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, LogisticsPipes.LogisticsSubMultiBlock.getUnlocalizedName()));
		event.getRegistry().register(LogisticsPipes.LogisticsSubMultiBlock);
	}

	public static <T extends LogisticsItem> T registerItem(T item) {
		MainProxy.proxy.registerModels(item);
		ForgeRegistries.ITEMS.register(item);
		return item;
	}

	private void registerRecipes() {
		/*
		CraftingParts parts = SimpleServiceLocator.buildCraftProxy.getRecipeParts();
		//NO BC => NO RECIPES (for now)
		if (parts != null) {
			SimpleServiceLocator.IC2Proxy.addCraftingRecipes(parts);
			SimpleServiceLocator.forestryProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.thaumCraftProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.ccProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.cofhPowerProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.buildCraftProxy.addCraftingRecipes(parts);

			SolderingStationRecipes.loadRecipe(parts);
			RecipeManager.loadRecipes();
		}
		parts = SimpleServiceLocator.thermalExpansionProxy.getRecipeParts();
		if (parts != null) {
			SimpleServiceLocator.cofhPowerProxy.addCraftingRecipes(parts);
		}
		*/

		if(true) { // TODO: Add Config Option
			CraftingPartRecipes.craftingPartList.add(new CraftingParts(
					new ItemStack(LogisticsPipes.LogisticsChips_fpga, 1),
					new ItemStack(LogisticsPipes.LogisticsChips_basic, 1),
					new ItemStack(LogisticsPipes.LogisticsChips_advanced, 1)));
			RecipeManager.recipeProvider.add(new LPChipRecipes());
		}
		RecipeManager.recipeProvider.add(new BlockChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new ModuleChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new PipeChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new ChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new CraftingRecipes());
		RecipeManager.loadRecipes();

	}

	private void loadClasses() {
		//Try to load all classes to let our checksums get generated
		forName("net.minecraft.tileentity.TileEntity");
		forName("net.minecraft.world.World");
		forName("net.minecraft.item.ItemStack");
		forName("net.minecraftforge.fluids.FluidStack");
		forName("net.minecraftforge.fluids.Fluid");
		forName("dan200.computercraft.core.lua.LuaJLuaMachine");
		forName("cofh.thermaldynamics.block.TileTDBase");
		forName("cofh.thermaldynamics.duct.item.TravelingItem");
		forName("cofh.thermaldynamics.render.RenderDuctItems");
		forName("crazypants.enderio.conduit.item.ItemConduit");
		forName("crazypants.enderio.conduit.item.NetworkedInventory");
		forName("crazypants.enderio.conduit.liquid.AbstractLiquidConduit");
	}

	private void forName(String string) {
		try {
			Class.forName(string);
		} catch (Exception ignore) {}
	}

	@Mod.EventHandler
	public void cleanup(FMLServerStoppingEvent event) {
		SimpleServiceLocator.routerManager.serverStopClean();
		QueuedTasks.clearAllTasks();
		HudUpdateTick.clearUpdateFlags();
		PipeItemsSatelliteLogistics.cleanup();
		PipeFluidSatellite.cleanup();
		ServerRouter.cleanup();
		if (event.getSide().equals(Side.CLIENT)) {
			LogisticsHUDRenderer.instance().clear();
		}
		LogisticsEventListener.serverShutdown();
		SimpleServiceLocator.buildCraftProxy.cleanup();
	}

	@Mod.EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new LogisticsPipesCommand());
	}

	@Mod.EventHandler
	public void certificateWarning(FMLFingerprintViolationEvent warning) {
		if (!LPConstants.DEBUG) {
			System.out.println("[LogisticsPipes|Certificate] Certificate not correct");
			System.out.println("[LogisticsPipes|Certificate] Expected: " + warning.getExpectedFingerprint());
			System.out.println("[LogisticsPipes|Certificate] File: " + warning.getSource().getAbsolutePath());
			System.out.println("[LogisticsPipes|Certificate] This in not a LogisticsPipes version from RS485.");
			certificateError = true;
		}
	}

	public static Object getComputerLP() {
		if (LogisticsPipes.generalAccess == null) {
			LogisticsPipes.generalAccess = new LPGlobalCCAccess();
		}
		return LogisticsPipes.generalAccess;
	}

	public void registerPipes() {
		LogisticsPipes.LogisticsBasicPipe = createPipe(PipeItemsBasicLogistics.class, "Basic Logistics Pipe");
		LogisticsPipes.LogisticsRequestPipeMk1 = createPipe(PipeItemsRequestLogistics.class, "Request Logistics Pipe");
		LogisticsPipes.LogisticsProviderPipeMk1 = createPipe(PipeItemsProviderLogistics.class, "Provider Logistics Pipe");
		LogisticsPipes.LogisticsCraftingPipeMk1 = createPipe(PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe");
		LogisticsPipes.LogisticsSatellitePipe = createPipe(PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe");
		LogisticsPipes.LogisticsSupplierPipe = createPipe(PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe");
		LogisticsPipes.LogisticsChassisPipeMk1 = createPipe(PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1");
		LogisticsPipes.LogisticsChassisPipeMk2 = createPipe(PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2");
		LogisticsPipes.LogisticsChassisPipeMk3 = createPipe(PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3");
		LogisticsPipes.LogisticsChassisPipeMk4 = createPipe(PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4");
		LogisticsPipes.LogisticsChassisPipeMk5 = createPipe(PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5");
		LogisticsPipes.LogisticsCraftingPipeMk2 = createPipe(PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2");
		LogisticsPipes.LogisticsRequestPipeMk2 = createPipe(PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2");
		LogisticsPipes.LogisticsRemoteOrdererPipe = createPipe(PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe");
		LogisticsPipes.LogisticsProviderPipeMk2 = createPipe(PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2");
		LogisticsPipes.LogisticsApiaristAnalyzerPipe = createPipe(PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe");
		LogisticsPipes.LogisticsApiaristSinkPipe = createPipe(PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe");
		LogisticsPipes.LogisticsInvSysConPipe = createPipe(PipeItemsInvSysConnector.class, "Logistics Inventory System Connector");
		LogisticsPipes.LogisticsEntrancePipe = createPipe(PipeItemsSystemEntranceLogistics.class, "Logistics System Entrance Pipe");
		LogisticsPipes.LogisticsDestinationPipe = createPipe(PipeItemsSystemDestinationLogistics.class, "Logistics System Destination Pipe");
		LogisticsPipes.LogisticsCraftingPipeMk3 = createPipe(PipeItemsCraftingLogisticsMk3.class, "Crafting Logistics Pipe MK3");
		LogisticsPipes.LogisticsFirewallPipe = createPipe(PipeItemsFirewall.class, "Firewall Logistics Pipe");

		LogisticsPipes.LogisticsFluidSupplierPipeMk1 = createPipe(PipeItemsFluidSupplier.class, "Fluid Supplier Logistics Pipe");

		LogisticsPipes.LogisticsFluidBasicPipe = createPipe(PipeFluidBasic.class, "Basic Logistics Fluid Pipe");
		LogisticsPipes.LogisticsFluidInsertionPipe = createPipe(PipeFluidInsertion.class, "Logistics Fluid Insertion Pipe");
		LogisticsPipes.LogisticsFluidProviderPipe = createPipe(PipeFluidProvider.class, "Logistics Fluid Provider Pipe");
		LogisticsPipes.LogisticsFluidRequestPipe = createPipe(PipeFluidRequestLogistics.class, "Logistics Fluid Request Pipe");
		LogisticsPipes.LogisticsFluidExtractorPipe = createPipe(PipeFluidExtractor.class, "Logistics Fluid Extractor Pipe");
		LogisticsPipes.LogisticsFluidSatellitePipe = createPipe(PipeFluidSatellite.class, "Logistics Fluid Satellite Pipe");
		LogisticsPipes.LogisticsFluidSupplierPipeMk2 = createPipe(PipeFluidSupplierMk2.class, "Logistics Fluid Supplier Pipe Mk2");

		LogisticsPipes.logisticsRequestTable = createPipe(PipeBlockRequestTable.class, "Request Table");

		LogisticsPipes.BasicTransportPipe = createPipe(PipeItemsBasicTransport.class, "Basic Transport Pipe");

		LogisticsPipes.HSTubeCurve = createPipe(HSTubeCurve.class, "High Speed Tube Curve");
		LogisticsPipes.HSTubeSpeedup = createPipe(HSTubeSpeedup.class, "High Speed Tube Speedup");
		LogisticsPipes.HSTubeSCurve = createPipe(HSTubeSCurve.class, "High Speed Tube S-Curve");
		LogisticsPipes.HSTubeLine = createPipe(HSTubeLine.class, "High Speed Tube Line");
		LogisticsPipes.HSTubeGain = createPipe(HSTubeGain.class, "High Speed Tube Gain");
	}

	protected Item createPipe(Class<? extends CoreUnroutedPipe> clas, String descr) {
		final ItemLogisticsPipe res = LogisticsBlockGenericPipe.registerPipe(clas);
		res.setCreativeTab(LogisticsPipes.LPCreativeTab);
		final CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.createPipe(res);
		if (pipe instanceof CoreRoutedPipe) {
			postInitRun.add(() -> res.setPipeIconIndex(((CoreRoutedPipe) pipe).getTextureType(null).normal, ((CoreRoutedPipe) pipe).getTextureType(null).newTexture));
		}

		if (clas != PipeItemsBasicLogistics.class && CoreRoutedPipe.class.isAssignableFrom(clas)) {
			if (clas != PipeFluidBasic.class && PipeFluidBasic.class.isAssignableFrom(clas)) {
				registerShapelessResetRecipe(res, 0, LogisticsPipes.LogisticsFluidBasicPipe, 0);
			} else {
				registerShapelessResetRecipe(res, 0, LogisticsPipes.LogisticsBasicPipe, 0);
			}
		}
		return res;
	}

	protected void registerShapelessResetRecipe(Item fromItem, int fromData, Item toItem, int toData) {
		for (int j = 1; j < 10; j++) {
			NonNullList<Ingredient> list = NonNullList.create();
			for (int k = 0; k < j; k++) {
				list.add(CraftingHelper.getIngredient(new ItemStack(fromItem, 1, toData)));
			}

			ItemStack output = new ItemStack(toItem, j, fromData);

			ResourceLocation baseLoc = new ResourceLocation(LPConstants.LP_MOD_ID, output.getItem().getRegistryName().getResourcePath());
			ResourceLocation recipeLoc = baseLoc;
			int index = 0;
			while (CraftingManager.REGISTRY.containsKey(recipeLoc)) {
				index++;
				recipeLoc = new ResourceLocation(LPConstants.LP_MOD_ID, baseLoc.getResourcePath() + "_" + index);
			}

			ShapelessRecipes recipe = new ShapelessRecipes(recipeLoc.getResourceDomain(), output, list);
			recipe.setRegistryName(recipeLoc);
			GameData.register_impl(recipe);
		}
	}

	public static PlayerConfig getClientPlayerConfig() {
		if (LogisticsPipes.playerConfig == null) {
			LogisticsPipes.playerConfig = new PlayerConfig(true, null);
		}
		return LogisticsPipes.playerConfig;
	}
}
