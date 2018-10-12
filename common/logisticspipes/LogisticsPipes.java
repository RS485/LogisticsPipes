/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.GameData;

import lombok.Getter;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;

import logisticspipes.asm.LogisticsPipesClassInjector;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.config.Configs;
import logisticspipes.config.PlayerConfig;
import logisticspipes.datafixer.LPDataFixer;
import logisticspipes.interfaces.ILogisticsItem;
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
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.pipes.tubes.HSTubeCurve;
import logisticspipes.pipes.tubes.HSTubeGain;
import logisticspipes.pipes.tubes.HSTubeLine;
import logisticspipes.pipes.tubes.HSTubeSCurve;
import logisticspipes.pipes.tubes.HSTubeSpeedup;
import logisticspipes.pipes.unrouted.PipeItemsBasicTransport;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
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
import logisticspipes.recipes.CraftingRecipes;
import logisticspipes.recipes.LPChipRecipes;
import logisticspipes.recipes.ModuleChippedCraftingRecipes;
import logisticspipes.recipes.PipeChippedCraftingRecipes;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.UpgradeChippedCraftingRecipes;
import logisticspipes.renderer.LogisticsHUDRenderer;
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
import logisticspipes.utils.StaticResolverUtil;
import logisticspipes.utils.TankUtilFactory;
import logisticspipes.utils.tuples.Pair;
import network.rs485.grow.TickExecutor;

//@formatter:off
//CHECKSTYLE:OFF

@Mod(
		modid = LPConstants.LP_MOD_ID,
		/* %------------CERTIFICATE-SUM-----------% */
		useMetadata = true)
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

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.Instance("logisticspipes")
	public static LogisticsPipes instance;

	@Getter
	private static TickExecutor globalTickExecutor;

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
	public static Map<Supplier<? extends LogisticsModule>, ItemModule> LogisticsModules = new HashMap<>();
	public static Map<Supplier<? extends IPipeUpgrade>, ItemUpgrade> LogisticsUpgrades = new HashMap<>();

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
	public static Logger log;
	public static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public static VersionChecker versionChecker;

	// initializes the creative tab
	public static final CreativeTabs CREATIVE_TAB_LP = new CreativeTabs("Logistics_Pipes") {

		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem() {
			return new ItemStack(LogisticsPipes.LogisticsBasicPipe);
		}
	};

	private Queue<Runnable> postInitRun = new LinkedList<>();
	private static LPGlobalCCAccess generalAccess;
	private static PlayerConfig playerConfig;

	private List<Pair<Item, Item>> resetRecipeList = new ArrayList<>();

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
		MinecraftForge.EVENT_BUS.register(new LPTickHandler());

		if (event.getSide().equals(Side.CLIENT)) {
			RenderTickHandler sub = new RenderTickHandler();
			MinecraftForge.EVENT_BUS.register(sub);
		}
		MinecraftForge.EVENT_BUS.register(new QueuedTasks());
		if (event.getSide() == Side.CLIENT) {
			SimpleServiceLocator.setClientPacketBufferHandlerThread(new ClientPacketBufferHandlerThread());
		}
		SimpleServiceLocator.setServerPacketBufferHandlerThread(new ServerPacketBufferHandlerThread());
		for (int i = 0; i < Configs.MULTI_THREAD_NUMBER; i++) {
			new RoutingTableUpdateThread(i);
		}
		LogisticsEventListener eventListener = new LogisticsEventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);
		MinecraftForge.EVENT_BUS.register(new LPChatListener());

		RecipeManager.registerRecipeClasses();
		LPDataFixer.INSTANCE.init();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		StaticResolverUtil.useASMDataTable(evt.getAsmData());
		PacketHandler.initialize();
		NewGuiHandler.initialize();

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

		MainProxy.proxy.initModelLoader();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		postInitRun.forEach(Runnable::run);
		postInitRun = null;

		SpecialInventoryHandlerManager.load();
		SpecialTankHandlerManager.load();

		SimpleServiceLocator.buildCraftProxy.registerPipeInformationProvider();
		SimpleServiceLocator.buildCraftProxy.initProxy();

		SimpleServiceLocator.thermalDynamicsProxy.registerPipeInformationProvider();

		//SimpleServiceLocator.specialpipeconnection.registerHandler(new TeleportPipes());
		//SimpleServiceLocator.specialtileconnection.registerHandler(new TesseractConnection());
		//SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOHyperCubeConnection());
		SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOTransceiverConnection());

		//SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Factory", "AutoWorkbench", AutoWorkbench.class));
		//SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Silicon", "AssemblyAdvancedWorkbench", AssemblyAdvancedWorkbench.class));
		if (SimpleServiceLocator.buildCraftProxy.getAssemblyTableProviderClass() != null) {
			SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider(LPConstants.bcSiliconModID, "AssemblyTable", SimpleServiceLocator.buildCraftProxy.getAssemblyTableProviderClass()));
		}
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider(LPConstants.railcraftModID, "RollingMachine", RollingMachine.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider(LPConstants.tubestuffModID, "ImmibisCraftingTableMk2", ImmibisCraftingTableMk2.class));
		SimpleServiceLocator.addCraftingRecipeProvider(new SolderingStation());
		SimpleServiceLocator.addCraftingRecipeProvider(new LogisticsCraftingTable());

		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.forestryModID, "Generic", ForestryProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.thermalExpansionModID, "Generic", ThermalExpansionProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.ic2ModID, "Generic", IC2ProgressProvider.class));
		//SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("EnderIO", "Generic", EnderIOProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.enderCoreModID, "Generic", EnderCoreProgressProvider.class));

		GameRegistry.registerTileEntity(LogisticsSolderingTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "soldering_station"));
		GameRegistry.registerTileEntity(LogisticsPowerJunctionTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "power_junction"));
		GameRegistry.registerTileEntity(LogisticsRFPowerProviderTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "power_provider_rf"));
		GameRegistry.registerTileEntity(LogisticsIC2PowerProviderTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "power_provider_ic2"));
		GameRegistry.registerTileEntity(LogisticsSecurityTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "security_station"));
		GameRegistry.registerTileEntity(LogisticsCraftingTableTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "logistics_crafting_table"));
		GameRegistry.registerTileEntity(LogisticsTileGenericPipe.class, new ResourceLocation(LPConstants.LP_MOD_ID, "pipe"));
		GameRegistry.registerTileEntity(LogisticsStatisticsTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "statistics_table"));
		GameRegistry.registerTileEntity(LogisticsProgramCompilerTileEntity.class, new ResourceLocation(LPConstants.LP_MOD_ID, "program_compiler"));
		GameRegistry.registerTileEntity(LogisticsTileGenericSubMultiBlock.class, new ResourceLocation(LPConstants.LP_MOD_ID, "submultiblock"));

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

//		LogisticsPipes.LogisticsItemCard = new LogisticsItemCard();
//		LogisticsPipes.LogisticsItemCard.setUnlocalizedName("logisticsItemCard");
//		LogisticsPipes.LogisticsItemCard.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticsitemcard"));
//		registerItem(LogisticsPipes.LogisticsItemCard);

		IForgeRegistry<Item> registry = event.getRegistry();

		registry.register(setName(new LogisticsItemCard(), "logisticsitemcard"));

//		LogisticsPipes.LogisticsRemoteOrderer = new RemoteOrderer();
//		LogisticsPipes.LogisticsRemoteOrderer.setUnlocalizedName("remoteOrdererItem");
//		LogisticsPipes.LogisticsRemoteOrderer.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "remoteordereritem"));
//		registerItem(LogisticsPipes.LogisticsRemoteOrderer);

		registry.register(setName(new RemoteOrderer(), "remoteordereritem"));

		ItemPipeSignCreator.registerPipeSignTypes();
//		LogisticsPipes.LogisticsCraftingSignCreator = new ItemPipeSignCreator();
//		LogisticsPipes.LogisticsCraftingSignCreator.setUnlocalizedName("ItemPipeSignCreator");
//		LogisticsPipes.LogisticsCraftingSignCreator.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "itempipesigncreator"));
//		registerItem(LogisticsPipes.LogisticsCraftingSignCreator);

		registry.register(setName(new ItemPipeSignCreator(), "itempipesigncreator"));

//		LogisticsPipes.LogisticsHUDArmor = new ItemHUDArmor();
//		LogisticsPipes.LogisticsHUDArmor.setUnlocalizedName("logisticsHUDGlasses");
//		LogisticsPipes.LogisticsHUDArmor.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticshudglasses"));
//		registerItem(LogisticsPipes.LogisticsHUDArmor);

		registry.register(setName(new ItemHUDArmor(), "logisticshudglasses"));

//		LogisticsPipes.LogisticsParts = registerItem(new ItemParts());
		registry.register(setName(new ItemParts(), "logisticsparts"));

//		LogisticsPipes.LogisticsBlankModule = registerItem(new ItemBlankModule());
		registry.register(setName(new ItemBlankModule(), "moduleblank"));

		ItemModule.loadModules(registry);

//		LogisticsPipes.LogisticsItemDisk = new ItemDisk();
//		LogisticsPipes.LogisticsItemDisk.setUnlocalizedName("itemDisk");
//		LogisticsPipes.LogisticsItemDisk.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "itemdisk"));
//		registerItem(LogisticsPipes.LogisticsItemDisk);
		registry.register(setName(new ItemDisk(), "itemdisk"));

		ItemUpgrade.loadUpgrades(registry);

//		LogisticsPipes.LogisticsFluidContainer = new LogisticsFluidContainer();
//		LogisticsPipes.LogisticsFluidContainer.setUnlocalizedName("logisticsFluidContainer");
//		LogisticsPipes.LogisticsFluidContainer.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticsfluidcontainer"));
//		registerItem(LogisticsPipes.LogisticsFluidContainer);
		registry.register(setName(new LogisticsFluidContainer(), "logisticsfluidcontainer"));

//		LogisticsPipes.LogisticsBrokenItem = new LogisticsBrokenItem();
//		LogisticsPipes.LogisticsBrokenItem.setUnlocalizedName("brokenItem");
//		LogisticsPipes.LogisticsBrokenItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "brokenitem"));
//		registerItem(LogisticsPipes.LogisticsBrokenItem);
		registry.register(setName(new LogisticsFluidContainer(), "brokenitem"));

//		LogisticsPipes.LogisticsPipeControllerItem = new ItemPipeController();
//		LogisticsPipes.LogisticsPipeControllerItem.setUnlocalizedName("pipeController");
//		LogisticsPipes.LogisticsPipeControllerItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "pipecontroller"));
//		registerItem(LogisticsPipes.LogisticsPipeControllerItem);
		registry.register(setName(new LogisticsFluidContainer(), "pipecontroller"));

//		LogisticsPipes.LogisticsPipeManagerItem = new ItemPipeManager();
//		LogisticsPipes.LogisticsPipeManagerItem.setUnlocalizedName("pipeManager");
//		LogisticsPipes.LogisticsPipeManagerItem.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "pipemanager"));
//		registerItem(LogisticsPipes.LogisticsPipeManagerItem);
		registry.register(setName(new ItemPipeManager(), "pipemanager"));

//		LogisticsPipes.LogisticsProgrammer = new ItemLogisticsProgrammer();
//		LogisticsPipes.LogisticsProgrammer.setUnlocalizedName("logisticsProgrammer");
//		LogisticsPipes.LogisticsProgrammer.setRegistryName(new ResourceLocation(LPConstants.LP_MOD_ID, "logisticsprogrammer"));
//		registerItem(LogisticsPipes.LogisticsProgrammer);
		registry.register(setName(new ItemLogisticsProgrammer(), "logisticsprogrammer"));

//		LogisticsChips_basic = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC));
//		LogisticsChips_basic_raw = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC_RAW));
//		LogisticsChips_advanced = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED));
//		LogisticsChips_advanced_raw = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED_RAW));
//		LogisticsChips_fpga = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA));
//		LogisticsChips_fpga_raw = registerItem(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA_RAW));
		registry.register(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC));
		registry.register(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC_RAW));
		registry.register(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED));
		registry.register(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED_RAW));
		registry.register(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA));
		registry.register(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA_RAW));


		registry.register(MainProxy.proxy.registerSolidBlockModel(new LogisticsSolidBlockItem(LogisticsPipes.LogisticsSolidBlock)).setRegistryName(LogisticsPipes.LogisticsSolidBlock.getRegistryName()));
		//event.getRegistry().register(new ItemBlock(LogisticsPipes.LogisticsPipeBlock).setRegistryName(LogisticsPipes.LogisticsPipeBlock.getRegistryName()));
		//event.getRegistry().register(new ItemBlock(LogisticsPipes.LogisticsSubMultiBlock).setRegistryName(LogisticsPipes.LogisticsSubMultiBlock.getRegistryName()));

		registerPipes(registry);
		registerRecipes(); // TODO data fileS!!!!!
	}

	// TODO move somewhere
	public static <T extends Item> T setName(T item, String name) {
		item.setRegistryName(LPConstants.LP_MOD_ID, name);
		item.setUnlocalizedName(String.format("%s.%s", LPConstants.LP_MOD_ID, name));
		return item;
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

	@Deprecated
	private static <T extends Item & ILogisticsItem> void registerItem(T item) {
		MainProxy.proxy.registerModels(item);
		ForgeRegistries.ITEMS.register(item);
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
			SimpleServiceLocator.buildCraftProxy.addCraftingRecipes(parts);

			SolderingStationRecipes.loadRecipe(parts);
			RecipeManager.loadRecipes();
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
		RecipeManager.recipeProvider.add(new UpgradeChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new ModuleChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new PipeChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new ChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new CraftingRecipes());
		RecipeManager.loadRecipes();

		resetRecipeList.forEach(itemItemPair -> registerShapelessResetRecipe(itemItemPair.getValue1(), itemItemPair.getValue2()));
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
	public void starting(FMLServerStartingEvent event) {
		globalTickExecutor = new TickExecutor();
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
		if (globalTickExecutor != null) {
			globalTickExecutor.shutdownNow();
		}
		LogisticsEventListener.serverShutdown();
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

	public void registerPipes(IForgeRegistry<Item> registry) {
		createPipe(registry, "basic", PipeItemsBasicLogistics::new, "Basic Logistics Pipe");
		createPipe(registry, "request", PipeItemsRequestLogistics::new, "Request Logistics Pipe");
		createPipe(registry, "provider", PipeItemsProviderLogistics::new, "Provider Logistics Pipe");
		createPipe(registry, "crafting", PipeItemsCraftingLogistics::new, "Crafting Logistics Pipe");
		createPipe(registry, "satellite", PipeItemsSatelliteLogistics::new, "Satellite Logistics Pipe");
		createPipe(registry, "supplier", PipeItemsSupplierLogistics::new, "Supplier Logistics Pipe");
		createPipe(registry, "chassis_mk1", PipeLogisticsChassiMk1::new, "Logistics Chassi Mk1");
		createPipe(registry, "chassis_mk2", PipeLogisticsChassiMk2::new, "Logistics Chassi Mk2");
		createPipe(registry, "chassis_mk3", PipeLogisticsChassiMk3::new, "Logistics Chassi Mk3");
		createPipe(registry, "chassis_mk4", PipeLogisticsChassiMk4::new, "Logistics Chassi Mk4");
		createPipe(registry, "chassis_mk5", PipeLogisticsChassiMk5::new, "Logistics Chassi Mk5");
		createPipe(registry, "crafting_mk2", PipeItemsCraftingLogisticsMk2::new, "Crafting Logistics Pipe MK2");
		createPipe(registry, "request_mk2", PipeItemsRequestLogisticsMk2::new, "Request Logistics Pipe MK2");
		createPipe(registry, "remote_orderer", PipeItemsRemoteOrdererLogistics::new, "Remote Orderer Pipe");
		createPipe(registry, "provider_mk2", PipeItemsProviderLogisticsMk2::new, "Provider Logistics Pipe MK2");
		createPipe(registry, "apiarist_analyser", PipeItemsApiaristAnalyser::new, "Apiarist Logistics Analyser Pipe");
		createPipe(registry, "apiarist_sink", PipeItemsApiaristSink::new, "Apiarist Logistics Analyser Pipe");
		createPipe(registry, "inventory_system_connector", PipeItemsInvSysConnector::new, "Logistics Inventory System Connector");
		createPipe(registry, "system_entrance", PipeItemsSystemEntranceLogistics::new, "Logistics System Entrance Pipe");
		createPipe(registry, "system_destination", PipeItemsSystemDestinationLogistics::new, "Logistics System Destination Pipe");
		createPipe(registry, "crafting_mk3", PipeItemsCraftingLogisticsMk3::new, "Crafting Logistics Pipe MK3");
		createPipe(registry, "firewall", PipeItemsFirewall::new, "Firewall Logistics Pipe");

		createPipe(registry, "fluid_supplier", PipeItemsFluidSupplier::new, "Fluid Supplier Logistics Pipe");

		createPipe(registry, "fluid_basic", PipeFluidBasic::new, "Basic Logistics Fluid Pipe");
		createPipe(registry, "fluid_insertion", PipeFluidInsertion::new, "Logistics Fluid Insertion Pipe");
		createPipe(registry, "fluid_provider", PipeFluidProvider::new, "Logistics Fluid Provider Pipe");
		createPipe(registry, "fluid_request", PipeFluidRequestLogistics::new, "Logistics Fluid Request Pipe");
		createPipe(registry, "fluid_extractor", PipeFluidExtractor::new, "Logistics Fluid Extractor Pipe");
		createPipe(registry, "fluid_satellite", PipeFluidSatellite::new, "Logistics Fluid Satellite Pipe");
		createPipe(registry, "fluid_supplier_mk2", PipeFluidSupplierMk2::new, "Logistics Fluid Supplier Pipe Mk2");

		createPipe(registry, "request_table", PipeBlockRequestTable::new, "Request Table");

		createPipe(registry, "unrouted", PipeItemsBasicTransport::new, "Basic Transport Pipe");

		createPipe(registry, "hs_curve", HSTubeCurve::new, "High Speed Tube Curve");
		createPipe(registry, "hs_speedup", HSTubeSpeedup::new, "High Speed Tube Speedup");
		createPipe(registry, "hs_s_curve", HSTubeSCurve::new, "High Speed Tube S-Curve");
		createPipe(registry, "hs_line", HSTubeLine::new, "High Speed Tube Line");
		createPipe(registry, "hs_gain", HSTubeGain::new, "High Speed Tube Gain");
	}

	protected void createPipe(IForgeRegistry<Item> registry, String name, Function<Item, ? extends CoreUnroutedPipe> constructor, String descr) {
		final ItemLogisticsPipe res = LogisticsBlockGenericPipe.registerPipe(registry, name, constructor);
		final CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.createPipe(res);
		if (pipe instanceof CoreRoutedPipe) {
			postInitRun.add(() -> res.setPipeIconIndex(((CoreRoutedPipe) pipe).getTextureType(null).normal, ((CoreRoutedPipe) pipe).getTextureType(null).newTexture));
		}

		if (pipe.getClass() != PipeItemsBasicLogistics.class && CoreRoutedPipe.class.isAssignableFrom(pipe.getClass())) {
			if (pipe.getClass() != PipeFluidBasic.class && PipeFluidBasic.class.isAssignableFrom(pipe.getClass())) {
				resetRecipeList.add(new Pair<>(res, LogisticsPipes.LogisticsFluidBasicPipe));
			} else {
				resetRecipeList.add(new Pair<>(res, LogisticsPipes.LogisticsBasicPipe));
			}
		}
	}

	protected void registerShapelessResetRecipe(Item fromItem, Item toItem) {
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(CraftingHelper.getIngredient(new ItemStack(fromItem, 1, 0)));

		ItemStack output = new ItemStack(toItem, 1, 0);

		ResourceLocation baseLoc = new ResourceLocation(LPConstants.LP_MOD_ID, fromItem.getRegistryName().getResourcePath() + ".resetrecipe");
		ResourceLocation recipeLoc = baseLoc;
		int index = 0;
		while (CraftingManager.REGISTRY.containsKey(recipeLoc)) {
			index++;
			recipeLoc = new ResourceLocation(LPConstants.LP_MOD_ID, baseLoc.getResourcePath() + "_" + index);
		}

		ShapelessRecipes recipe = new ShapelessRecipes("logisticspipes.resetrecipe.pipe", output, list);
		recipe.setRegistryName(recipeLoc);
		GameData.register_impl(recipe);
	}

	public static PlayerConfig getClientPlayerConfig() {
		if (LogisticsPipes.playerConfig == null) {
			LogisticsPipes.playerConfig = new PlayerConfig(true, null);
		}
		return LogisticsPipes.playerConfig;
	}
}
