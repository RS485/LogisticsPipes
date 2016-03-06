/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import logisticspipes.asm.LogisticsPipesClassInjector;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.config.Configs;
import logisticspipes.config.PlayerConfig;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemParts;
import logisticspipes.items.ItemPipeComponents;
import logisticspipes.items.ItemPipeController;
import logisticspipes.items.ItemPipeSignCreator;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsBrokenItem;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logistics.LogisticsFluidManager;
import logisticspipes.logistics.LogisticsManager;
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
import logisticspipes.pipes.unrouted.PipeItemsBasicTransport;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.ProxyManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.SpecialInventoryHandlerManager;
import logisticspipes.proxy.SpecialTankHandlerManager;
import logisticspipes.proxy.computers.objects.LPGlobalCCAccess;
import logisticspipes.proxy.endercore.EnderCoreProgressProvider;
import logisticspipes.proxy.enderio.EnderIOProgressProvider;
import logisticspipes.proxy.forestry.ForestryProgressProvider;
import logisticspipes.proxy.ic2.IC2ProgressProvider;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.proxy.progressprovider.MachineProgressProvider;
import logisticspipes.proxy.recipeproviders.AssemblyAdvancedWorkbench;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.ImmibisCraftingTableMk2;
import logisticspipes.proxy.recipeproviders.LogisticsCraftingTable;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.proxy.recipeproviders.SolderingStation;
import logisticspipes.proxy.specialconnection.EnderIOHyperCubeConnection;
import logisticspipes.proxy.specialconnection.EnderIOTransceiverConnection;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.proxy.specialconnection.TeleportPipes;
import logisticspipes.proxy.specialconnection.TesseractConnection;
import logisticspipes.proxy.specialtankhandler.SpecialTankHandler;
import logisticspipes.proxy.te.ThermalExpansionProgressProvider;
import logisticspipes.recipes.CraftingPermissionManager;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.renderer.FluidContainerRenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.renderer.LogisticsPipeItemRenderer;
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

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

import org.apache.logging.log4j.Logger;

//@formatter:off
//CHECKSTYLE:OFF

@Mod(
		modid = "LogisticsPipes",
		name = "Logistics Pipes",
		version = "%VERSION%",
		/* %------------CERTIFICATE-SUM-----------% */
		dependencies = "required-after:Forge@[10.12.1.1079,);" +
				"required-after:BuildCraft|Core;" +
				"required-after:BuildCraft|Transport;" +
				"required-after:BuildCraft|Silicon;" +
				"required-after:BuildCraft|Robotics;" +
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
public class LogisticsPipes {

	//@formatter:on
	//CHECKSTYLE:ON

	public LogisticsPipes() {
		LaunchClassLoader loader = Launch.classLoader;
		if (!LPConstants.COREMOD_LOADED) {
			if (LPConstants.DEBUG) {
				throw new RuntimeException("LogisticsPipes FMLLoadingPlugin wasn't loaded. If you are running MC from an IDE make sure to copy the 'LogisticsPipes_dummy.jar' to your mods folder. If you are running MC normal please report this as a bug at 'https://github.com/RS485/LogisticsPipes/issues'.");
			} else {
				throw new RuntimeException("LogisticsPipes FMLLoadingPlugin wasn't loaded. Your download seems to be corrupt/modified. Please redownload LP from our Jenkins [http://ci.thezorro266.com/] and move it into your mods folder.");
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
		} catch (NoSuchFieldException e) {
			loader.registerTransformer("logisticspipes.asm.LogisticsPipesClassInjector");
			e.printStackTrace();
		} catch (SecurityException e) {
			loader.registerTransformer("logisticspipes.asm.LogisticsPipesClassInjector");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			loader.registerTransformer("logisticspipes.asm.LogisticsPipesClassInjector");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			loader.registerTransformer("logisticspipes.asm.LogisticsPipesClassInjector");
			e.printStackTrace();
		}
		PacketHandler.initialize();
		NewGuiHandler.initialize();
	}

	@Instance("LogisticsPipes")
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

	// Logistics Modules/Upgrades
	public static ItemModule ModuleItem;
	public static ItemUpgrade UpgradeItem;

	// Miscellaneous Items
	public static Item LogisticsRemoteOrderer;
	public static Item LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static Item LogisticsItemCard;
	public static ItemHUDArmor LogisticsHUDArmor;
	public static Item LogisticsParts;
	public static Item LogisticsPipeComponents;
	public static Item LogisticsFluidContainer;
	public static Item LogisticsBrokenItem;
	public static Item LogisticsPipeControllerItem;

	// Logistics Blocks
	public static Block LogisticsSolidBlock;
	public static LogisticsBlockGenericPipe LogisticsPipeBlock;

	// other statics
	public static Textures textures = new Textures();
	public static final String logisticsTileGenericPipeMapping = "logisticspipes.pipes.basic.LogisticsTileGenericPipe";
	public static CreativeTabLP LPCreativeTab = new CreativeTabLP();
	public static Logger log;
	public static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public static VersionChecker versionChecker;

	private Queue<Runnable> postInitRun = new LinkedList<Runnable>();
	private static LPGlobalCCAccess generalAccess;
	private static PlayerConfig playerConfig;

	@EventHandler
	public void init(FMLInitializationEvent event) {

		//Register Network channels
		MainProxy.createChannels();

		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setSecurityStationManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManager());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialPipeConnection());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialTileConnection());
		SimpleServiceLocator.setLogisticsFluidManager(new LogisticsFluidManager());
		SimpleServiceLocator.setSpecialTankHandler(new SpecialTankHandler());
		SimpleServiceLocator.setCraftingPermissionManager(new CraftingPermissionManager());
		SimpleServiceLocator.setMachineProgressProvider(new MachineProgressProvider());
		SimpleServiceLocator.setRoutedItemHelper(new RoutedItemHelper());

		NetworkRegistry.INSTANCE.registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		FMLCommonHandler.instance().bus().register(new LPTickHandler());

		if (event.getSide().equals(Side.CLIENT)) {
			FMLCommonHandler.instance().bus().register(new RenderTickHandler());
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
		LogisticsPipes.textures.registerBlockIcons(null);

		RecipeManager.registerRecipeClasses();

		registerRecipes();
	}

	@EventHandler
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

		if (Configs.EASTER_EGGS) {
			Calendar calendar = Calendar.getInstance();
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			if (month == Calendar.OCTOBER && day == 1) { //GUIpsp's birthday.
				Items.slime_ball.setTextureName("logisticspipes:eastereggs/guipsp");
			}
		}

		initItems(evt.getSide());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		while(!postInitRun.isEmpty()) {
			postInitRun.poll().run();
		}
		postInitRun = null;

		SpecialInventoryHandlerManager.load();
		SpecialTankHandlerManager.load();

		SimpleServiceLocator.buildCraftProxy.registerPipeInformationProvider();
		SimpleServiceLocator.buildCraftProxy.initProxy();
		SimpleServiceLocator.thermalDynamicsProxy.registerPipeInformationProvider();

		SimpleServiceLocator.specialpipeconnection.registerHandler(new TeleportPipes());
		SimpleServiceLocator.specialtileconnection.registerHandler(new TesseractConnection());
		SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOHyperCubeConnection());
		SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOTransceiverConnection());

		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Factory", "AutoWorkbench", AutoWorkbench.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Silicon", "AssemblyAdvancedWorkbench", AssemblyAdvancedWorkbench.class));
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
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("EnderIO", "Generic", EnderIOProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("endercore", "Generic", EnderCoreProgressProvider.class));

		MainProxy.proxy.registerTileEntities();

		//Registering special particles
		MainProxy.proxy.registerParticles();

		//init Fluids
		FluidIdentifier.initFromForge(false);

		versionChecker = VersionChecker.runVersionCheck();
	}

	private void initItems(Side side) {

		boolean isClient = side == Side.CLIENT;

		Object renderer = null;
		if (isClient) {
			renderer = new FluidContainerRenderer();
		}

		LogisticsPipes.LogisticsItemCard = new LogisticsItemCard();
		LogisticsPipes.LogisticsItemCard.setUnlocalizedName("logisticsItemCard");
		GameRegistry.registerItem(LogisticsPipes.LogisticsItemCard, LogisticsPipes.LogisticsItemCard.getUnlocalizedName());
		if (isClient) {
			MinecraftForgeClient.registerItemRenderer(LogisticsPipes.LogisticsItemCard, (FluidContainerRenderer) renderer);
		}

		LogisticsPipes.LogisticsRemoteOrderer = new RemoteOrderer();
		LogisticsPipes.LogisticsRemoteOrderer.setUnlocalizedName("remoteOrdererItem");
		GameRegistry.registerItem(LogisticsPipes.LogisticsRemoteOrderer, LogisticsPipes.LogisticsRemoteOrderer.getUnlocalizedName());

		ItemPipeSignCreator.registerPipeSignTypes();
		LogisticsPipes.LogisticsCraftingSignCreator = new ItemPipeSignCreator();
		LogisticsPipes.LogisticsCraftingSignCreator.setUnlocalizedName("ItemPipeSignCreator");
		GameRegistry.registerItem(LogisticsPipes.LogisticsCraftingSignCreator, LogisticsPipes.LogisticsCraftingSignCreator.getUnlocalizedName());

		int renderIndex;
		if (isClient) {
			renderIndex = RenderingRegistry.addNewArmourRendererPrefix("LogisticsHUD");
		} else {
			renderIndex = 0;
		}
		LogisticsPipes.LogisticsHUDArmor = new ItemHUDArmor(renderIndex);
		LogisticsPipes.LogisticsHUDArmor.setUnlocalizedName("logisticsHUDGlasses");
		GameRegistry.registerItem(LogisticsPipes.LogisticsHUDArmor, LogisticsPipes.LogisticsHUDArmor.getUnlocalizedName());

		LogisticsPipes.LogisticsParts = new ItemParts();
		LogisticsPipes.LogisticsParts.setUnlocalizedName("logisticsParts");
		GameRegistry.registerItem(LogisticsPipes.LogisticsParts, LogisticsPipes.LogisticsParts.getUnlocalizedName());

		LogisticsPipes.LogisticsPipeComponents = new ItemPipeComponents();
		LogisticsPipes.LogisticsPipeComponents.setUnlocalizedName("pipeComponents");
		GameRegistry.registerItem(LogisticsPipes.LogisticsPipeComponents, LogisticsPipes.LogisticsPipeComponents.getUnlocalizedName());

		SimpleServiceLocator.buildCraftProxy.registerTrigger();

		LogisticsPipes.ModuleItem = new ItemModule();
		LogisticsPipes.ModuleItem.setUnlocalizedName("itemModule");
		LogisticsPipes.ModuleItem.loadModules();
		GameRegistry.registerItem(LogisticsPipes.ModuleItem, LogisticsPipes.ModuleItem.getUnlocalizedName());

		LogisticsPipes.LogisticsItemDisk = new ItemDisk();
		LogisticsPipes.LogisticsItemDisk.setUnlocalizedName("itemDisk");
		GameRegistry.registerItem(LogisticsPipes.LogisticsItemDisk, LogisticsPipes.LogisticsItemDisk.getUnlocalizedName());

		LogisticsPipes.UpgradeItem = new ItemUpgrade();
		LogisticsPipes.UpgradeItem.setUnlocalizedName("itemUpgrade");
		LogisticsPipes.UpgradeItem.loadUpgrades();
		GameRegistry.registerItem(LogisticsPipes.UpgradeItem, LogisticsPipes.UpgradeItem.getUnlocalizedName());

		LogisticsPipes.LogisticsFluidContainer = new LogisticsFluidContainer();
		LogisticsPipes.LogisticsFluidContainer.setUnlocalizedName("logisticsFluidContainer");
		if (isClient) {
			MinecraftForgeClient.registerItemRenderer(LogisticsPipes.LogisticsFluidContainer, (FluidContainerRenderer) renderer);
		}
		GameRegistry.registerItem(LogisticsPipes.LogisticsFluidContainer, LogisticsPipes.LogisticsFluidContainer.getUnlocalizedName());

		LogisticsPipes.LogisticsBrokenItem = new LogisticsBrokenItem();
		LogisticsPipes.LogisticsBrokenItem.setUnlocalizedName("brokenItem");
		GameRegistry.registerItem(LogisticsPipes.LogisticsBrokenItem, LogisticsPipes.LogisticsBrokenItem.getUnlocalizedName());

		LogisticsPipes.LogisticsPipeControllerItem = new ItemPipeController();
		LogisticsPipes.LogisticsPipeControllerItem.setUnlocalizedName("pipeController");
		GameRegistry.registerItem(LogisticsPipes.LogisticsPipeControllerItem, LogisticsPipes.LogisticsPipeControllerItem.getUnlocalizedName());

		//Blocks
		LogisticsPipes.LogisticsSolidBlock = new LogisticsSolidBlock();
		GameRegistry.registerBlock(LogisticsPipes.LogisticsSolidBlock, LogisticsSolidBlockItem.class, "logisticsSolidBlock");

		LogisticsPipes.LogisticsPipeBlock = new LogisticsBlockGenericPipe();
		GameRegistry.registerBlock(LogisticsPipes.LogisticsPipeBlock, "logisticsPipeBlock");

		registerPipes(side);
	}

	private void registerRecipes() {
		ICraftingParts parts = SimpleServiceLocator.buildCraftProxy.getRecipeParts();
		//NO BC => NO RECIPES (for now)
		if (parts != null) {
			SimpleServiceLocator.IC2Proxy.addCraftingRecipes(parts);
			SimpleServiceLocator.forestryProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.thaumCraftProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.ccProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.cofhPowerProxy.addCraftingRecipes(parts);
			SimpleServiceLocator.buildCraftProxy.addCraftingRecipes(parts);

			SolderingStationRecipes.loadRecipe(parts);
			RecipeManager.loadRecipes(parts);
		}
		parts = SimpleServiceLocator.thermalExpansionProxy.getRecipeParts();
		if (parts != null) {
			SimpleServiceLocator.cofhPowerProxy.addCraftingRecipes(parts);
		}
	}

	private void loadClasses() {
		//Try to load all classes to let out checksums get generated
		forName("net.minecraft.tileentity.TileEntity");
		forName("net.minecraft.world.World");
		forName("net.minecraft.item.ItemStack");
		forName("net.minecraftforge.fluids.FluidStack");
		forName("net.minecraftforge.fluids.Fluid");
		forName("dan200.computercraft.core.lua.LuaJLuaMachine");
		forName("cofh.thermaldynamics.block.TileTDBase");
		forName("cofh.thermaldynamics.duct.item.TravelingItem");
		forName("cofh.thermaldynamics.render.RenderDuctItems");
	}

	private void forName(String string) {
		try {
			Class.forName(string);
		} catch (Exception ignore) {}
	}

	@EventHandler
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

	@EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new LogisticsPipesCommand());
	}

	@EventHandler
	public void certificateWarning(FMLFingerprintViolationEvent warning) {
		if (!LPConstants.DEBUG) {
			System.out.println("[LogisticsPipes|Certificate] Certificate not correct");
			System.out.println("[LogisticsPipes|Certificate] Expected: " + warning.expectedFingerprint);
			System.out.println("[LogisticsPipes|Certificate] File: " + warning.source.getAbsolutePath());
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

	public void registerPipes(Side side) {
		LogisticsPipes.LogisticsBasicPipe = createPipe(PipeItemsBasicLogistics.class, "Basic Logistics Pipe", side);
		LogisticsPipes.LogisticsRequestPipeMk1 = createPipe(PipeItemsRequestLogistics.class, "Request Logistics Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk1 = createPipe(PipeItemsProviderLogistics.class, "Provider Logistics Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk1 = createPipe(PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", side);
		LogisticsPipes.LogisticsSatellitePipe = createPipe(PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", side);
		LogisticsPipes.LogisticsSupplierPipe = createPipe(PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", side);
		LogisticsPipes.LogisticsChassisPipeMk1 = createPipe(PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", side);
		LogisticsPipes.LogisticsChassisPipeMk2 = createPipe(PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", side);
		LogisticsPipes.LogisticsChassisPipeMk3 = createPipe(PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", side);
		LogisticsPipes.LogisticsChassisPipeMk4 = createPipe(PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", side);
		LogisticsPipes.LogisticsChassisPipeMk5 = createPipe(PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", side);
		LogisticsPipes.LogisticsCraftingPipeMk2 = createPipe(PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRequestPipeMk2 = createPipe(PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRemoteOrdererPipe = createPipe(PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk2 = createPipe(PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsApiaristAnalyzerPipe = createPipe(PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsApiaristSinkPipe = createPipe(PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsInvSysConPipe = createPipe(PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", side);
		LogisticsPipes.LogisticsEntrancePipe = createPipe(PipeItemsSystemEntranceLogistics.class, "Logistics System Entrance Pipe", side);
		LogisticsPipes.LogisticsDestinationPipe = createPipe(PipeItemsSystemDestinationLogistics.class, "Logistics System Destination Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk3 = createPipe(PipeItemsCraftingLogisticsMk3.class, "Crafting Logistics Pipe MK3", side);
		LogisticsPipes.LogisticsFirewallPipe = createPipe(PipeItemsFirewall.class, "Firewall Logistics Pipe", side);

		LogisticsPipes.LogisticsFluidSupplierPipeMk1 = createPipe(PipeItemsFluidSupplier.class, "Fluid Supplier Logistics Pipe", side);

		LogisticsPipes.LogisticsFluidBasicPipe = createPipe(PipeFluidBasic.class, "Basic Logistics Fluid Pipe", side);
		LogisticsPipes.LogisticsFluidInsertionPipe = createPipe(PipeFluidInsertion.class, "Logistics Fluid Insertion Pipe", side);
		LogisticsPipes.LogisticsFluidProviderPipe = createPipe(PipeFluidProvider.class, "Logistics Fluid Provider Pipe", side);
		LogisticsPipes.LogisticsFluidRequestPipe = createPipe(PipeFluidRequestLogistics.class, "Logistics Fluid Request Pipe", side);
		LogisticsPipes.LogisticsFluidExtractorPipe = createPipe(PipeFluidExtractor.class, "Logistics Fluid Extractor Pipe", side);
		LogisticsPipes.LogisticsFluidSatellitePipe = createPipe(PipeFluidSatellite.class, "Logistics Fluid Satellite Pipe", side);
		LogisticsPipes.LogisticsFluidSupplierPipeMk2 = createPipe(PipeFluidSupplierMk2.class, "Logistics Fluid Supplier Pipe Mk2", side);

		LogisticsPipes.logisticsRequestTable = createPipe(PipeBlockRequestTable.class, "Request Table", side);

		LogisticsPipes.BasicTransportPipe = createPipe(PipeItemsBasicTransport.class, "Basic Transport Pipe", side);
	}

	protected Item createPipe(Class<? extends CoreUnroutedPipe> clas, String descr, Side side) {
		final ItemLogisticsPipe res = LogisticsBlockGenericPipe.registerPipe(clas);
		res.setCreativeTab(LogisticsPipes.LPCreativeTab);
		res.setUnlocalizedName(clas.getSimpleName());
		final CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.createPipe(res);
		if (pipe instanceof CoreRoutedPipe) {
			postInitRun.add(new Runnable() {
				@Override
				public void run() {
					res.setPipeIconIndex(((CoreRoutedPipe) pipe).getTextureType(ForgeDirection.UNKNOWN).normal, ((CoreRoutedPipe) pipe).getTextureType(ForgeDirection.UNKNOWN).newTexture);
				}
			});
		}

		if (side.isClient()) {
			if (pipe instanceof PipeBlockRequestTable) {
				MinecraftForgeClient.registerItemRenderer(res, new LogisticsPipeItemRenderer(true));
			} else {
				MinecraftForgeClient.registerItemRenderer(res, MainProxy.proxy.getPipeItemRenderer());
			}
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
			Object[] obj = new Object[j];
			for (int k = 0; k < j; k++) {
				obj[k] = new ItemStack(fromItem, 1, toData);
			}
			CraftingManager.getInstance().addShapelessRecipe(new ItemStack(toItem, j, fromData), obj);
		}
	}

	public static PlayerConfig getClientPlayerConfig() {
		if (LogisticsPipes.playerConfig == null) {
			LogisticsPipes.playerConfig = new PlayerConfig(true, null);
		}
		return LogisticsPipes.playerConfig;
	}
}
