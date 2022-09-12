/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

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

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;

import logisticspipes.asm.LogisticsPipesClassInjector;
import logisticspipes.asm.LogisticsPipesCoreLoader;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.BlockDummy;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.config.Configs;
import logisticspipes.datafixer.LPDataFixer;
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
import logisticspipes.pipes.PipeFluidTerminus;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLogisticsChassisMk1;
import logisticspipes.pipes.PipeLogisticsChassisMk2;
import logisticspipes.pipes.PipeLogisticsChassisMk3;
import logisticspipes.pipes.PipeLogisticsChassisMk4;
import logisticspipes.pipes.PipeLogisticsChassisMk5;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericSubMultiBlock;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
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
import logisticspipes.proxy.ic2.IC2ProgressProvider;
import logisticspipes.proxy.progressprovider.MachineProgressProvider;
import logisticspipes.proxy.recipeproviders.LogisticsCraftingTable;
import logisticspipes.proxy.specialconnection.EnderIOTransceiverConnection;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.proxy.specialtankhandler.SpecialTankHandler;
import logisticspipes.proxy.te.ThermalExpansionProgressProvider;
import logisticspipes.recipes.CraftingRecipes;
import logisticspipes.recipes.LPChipRecipes;
import logisticspipes.recipes.ModuleChippedCraftingRecipes;
import logisticspipes.recipes.PipeChippedCraftingRecipes;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.UpgradeChippedCraftingRecipes;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.renderer.newpipe.LogisticsNewSolidBlockWorldRenderer;
import logisticspipes.renderer.newpipe.tube.CurveTubeRenderer;
import logisticspipes.renderer.newpipe.tube.GainTubeRenderer;
import logisticspipes.renderer.newpipe.tube.LineTubeRenderer;
import logisticspipes.renderer.newpipe.tube.SCurveTubeRenderer;
import logisticspipes.renderer.newpipe.tube.SpeedupTubeRenderer;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.channels.ChannelManagerProvider;
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
import logisticspipes.utils.tuples.Pair;
import network.rs485.grow.ServerTickDispatcher;
import network.rs485.logisticspipes.compat.TheOneProbeIntegration;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.config.ServerConfigurationManager;
import network.rs485.logisticspipes.gui.font.LPFontRenderer;
import network.rs485.logisticspipes.gui.PropertyUpdaterEventListener;
import network.rs485.logisticspipes.guidebook.ItemGuideBook;

//@formatter:off
//CHECKSTYLE:OFF

@Mod(
		name = "Logistics Pipes",
		modid = LPConstants.LP_MOD_ID,
		certificateFingerprint = "e0c86912b2f7cc0cc646ad57799574aea43dbd45",
		useMetadata = true)
public class LogisticsPipes {
	//@formatter:on
	//CHECKSTYLE:ON

	public static final String UNKNOWN = "unknown";
	private static boolean DEBUG = true;
	private Consumer<FMLServerStartedEvent> minecraftTestStartMethod = null;

	public static boolean isDEBUG() {
		return DEBUG;
	}

	@Getter
	private static String VERSION = UNKNOWN;
	@Getter
	private static String VENDOR = UNKNOWN;
	@Getter
	private static String TARGET = UNKNOWN;

	public LogisticsPipes() { //TODO: remove throws
		final LaunchClassLoader loader = Launch.classLoader;
		loadManifestValues(loader);

		if (!LogisticsPipesCoreLoader.isCoremodLoaded()) {
			if (LogisticsPipes.DEBUG) {
				throw new RuntimeException("LogisticsPipes FMLLoadingPlugin wasn't loaded. If you are running MC from an IDE make sure to add '-Dfml.coreMods.load=logisticspipes.asm.LogisticsPipesCoreLoader' to the VM arguments. If you are running MC normal please report this as a bug at 'https://github.com/RS485/LogisticsPipes/issues'.");
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

	private static void loadManifestValues(ClassLoader loader) {
		try {
			final Enumeration<URL> resources = loader.getResources(JarFile.MANIFEST_NAME);
			boolean foundLp;
			do {
				final Manifest manifest = new Manifest(resources.nextElement().openStream());
				foundLp = "LogisticsPipes".equals(manifest.getMainAttributes().getValue("Specification-Title"));
				if (foundLp) {
					LogisticsPipes.DEBUG = false;
					LogisticsPipes.VERSION = manifest.getMainAttributes().getValue("Implementation-Version");
					LogisticsPipes.VENDOR = manifest.getMainAttributes().getValue("Implementation-Vendor");
					LogisticsPipes.TARGET = manifest.getMainAttributes().getValue("Implementation-Target");
				}
			} while (resources.hasMoreElements() && !foundLp);
		} catch (IOException e) {
			LogisticsPipes.log.error("There was a problem loading our MANIFEST file, Logistics Pipes will not know about its origin");
		}
	}

	@Mod.Instance("logisticspipes")
	public static LogisticsPipes instance;

	private static boolean certificateError = false;

	public static String getVersionString() {
		return Stream.of(
				"Logistics Pipes " + LogisticsPipes.VERSION,
				LogisticsPipes.certificateError ? "certificate error" : "",
				LogisticsPipes.DEBUG ? "debug mode" : "",
				"target " + LogisticsPipes.TARGET,
				"vendor " + LogisticsPipes.VENDOR)
				.filter(str -> !str.isEmpty())
				.collect(Collectors.joining(", "));
	}

	// other statics
	public static Textures textures = new Textures();
	public static Logger log;
	public static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public static VersionChecker versionChecker;

	// initializes the creative tab
	public static final CreativeTabs CREATIVE_TAB_LP = new CreativeTabs("Logistics_Pipes") {

		@SideOnly(Side.CLIENT)
		@Nonnull
		public ItemStack getTabIconItem() {
			return new ItemStack(LPItems.pipeBasic);
		}
	};

	private Queue<Runnable> postInitRun = new LinkedList<>();
	private static LPGlobalCCAccess generalAccess;
	private static ClientConfiguration playerConfig;
	private static ServerConfigurationManager serverConfigManager;

	private List<Supplier<Pair<Item, Item>>> resetRecipeList = new ArrayList<>();

	@CapabilityInject(IItemHandler.class)
	public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;

	@CapabilityInject(IFluidHandler.class)
	public static Capability<IFluidHandler> FLUID_HANDLER_CAPABILITY = null;

	public static boolean isDevelopmentEnvironment() {
		if (!isDEBUG()) {
			return false;
		} else {
			boolean eclipseCheck = (new File(".classpath")).exists();
			boolean ideaCheck = System.getProperty("java.class.path").contains("idea_rt.jar");

			return eclipseCheck || ideaCheck;
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		registerRecipes(); // TODO data fileS!!!!!

		//Register Network channels
		MainProxy.createChannels();

		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setChannelConnectionManager(manager);
		SimpleServiceLocator.setSecurityStationManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManager());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialPipeConnection());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialTileConnection());
		SimpleServiceLocator.setSpecialTankHandler(new SpecialTankHandler());
		SimpleServiceLocator.setMachineProgressProvider(new MachineProgressProvider());
		SimpleServiceLocator.setRoutedItemHelper(new RoutedItemHelper());
		SimpleServiceLocator.setChannelManagerProvider(new ChannelManagerProvider());

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
		MinecraftForge.EVENT_BUS.register(new LogisticsEventListener());
		MinecraftForge.EVENT_BUS.register(new LPChatListener());
		MinecraftForge.EVENT_BUS.register(PropertyUpdaterEventListener.INSTANCE);

		LPDataFixer.INSTANCE.init();

		if (event.getSide() == Side.SERVER) {
			LogisticsPipes.textures.registerBlockIcons(null);
		} else if (event.getSide() == Side.CLIENT) {
			LPFontRenderer.Factory.asyncPreload();
		}

		// load all the models so they don't get loaded and crash on concurrent class loading
		// the OBJParser is a non-sharable static thing
		LogisticsNewRenderPipe.loadModels();
		LogisticsNewSolidBlockWorldRenderer.loadModels();
		CurveTubeRenderer.loadModels();
		GainTubeRenderer.loadModels();
		LineTubeRenderer.loadModels();
		SpeedupTubeRenderer.loadModels();
		SCurveTubeRenderer.loadModels();

		if (isTesting()) {
			final Class<?> testClass;
			try {
				testClass = Class.forName("network.rs485.logisticspipes.integration.MinecraftTest");
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Error loading minecraft test class", e);
			}
			final Object minecraftTestInstance;
			try {
				minecraftTestInstance = testClass.getDeclaredField("INSTANCE").get(null);
				final Method serverStartMethod = testClass
						.getDeclaredMethod("serverStart", FMLServerStartedEvent.class);
				minecraftTestStartMethod = (FMLServerStartedEvent serverStartedEvent) -> {
					try {
						serverStartMethod.invoke(minecraftTestInstance, serverStartedEvent);
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException("Could not run server started hook in " + minecraftTestInstance, e);
					}
				};
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Error accessing minecraft test instance", e);
			}

			MinecraftForge.EVENT_BUS.register(minecraftTestInstance);
		}
	}

	public static boolean isTesting() {
		final String testSetting = System.getProperty("logisticspipes.test");
		return testSetting != null && testSetting.equalsIgnoreCase("true");
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		StaticResolverUtil.useASMDataTable(evt.getAsmData());
		PacketHandler.initialize();
		NewGuiHandler.initialize();

		LogisticsPipes.log = evt.getModLog();
		log.info("====================================================");
		log.info(" LogisticsPipes Logger initialized, enabled levels: ");
		log.info("----------------------------------------------------");
		log.info("    Fatal: " + log.isFatalEnabled());
		log.info("    Error: " + log.isErrorEnabled());
		log.info("    Warn:  " + log.isWarnEnabled());
		log.info("    Info:  " + log.isInfoEnabled());
		log.info("    Trace: " + log.isTraceEnabled());
		log.info("    Debug: " + log.isDebugEnabled());
		log.info("====================================================");
		loadClasses();
		ProxyManager.load();
		Configs.load();
		if (LogisticsPipes.certificateError) {
			LogisticsPipes.log.fatal("Certificate not correct");
			LogisticsPipes.log.fatal("This in not a LogisticsPipes version from RS485.");
		}

		if (LogisticsPipes.UNKNOWN.equals(LogisticsPipes.VERSION)) {
			LogisticsPipes.log.warn("Could not determine Logistics Pipes version, we do need that " + JarFile.MANIFEST_NAME + ", don't you know?");
		}
		LogisticsPipes.log.info("Running " + getVersionString());

		SimpleServiceLocator.setPipeInformationManager(new PipeInformationManager());
		SimpleServiceLocator.setLogisticsFluidManager(new LogisticsFluidManager());

		if (Loader.isModLoaded(LPConstants.theOneProbeModID)) {
			FMLInterModComms.sendFunctionMessage(LPConstants.theOneProbeModID, "getTheOneProbe",
					TheOneProbeIntegration.class.getName());
		}

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
		SimpleServiceLocator.addCraftingRecipeProvider(new LogisticsCraftingTable());

		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.thermalExpansionModID, "Generic", ThermalExpansionProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.ic2ModID, "Generic", IC2ProgressProvider.class));
		//SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("EnderIO", "Generic", EnderIOProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider(LPConstants.enderCoreModID, "Generic", EnderCoreProgressProvider.class));

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

		SimpleServiceLocator.mcmpProxy.registerTileEntities();

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
		IForgeRegistry<Item> registry = event.getRegistry();

		ItemPipeSignCreator.registerPipeSignTypes();
		ItemModule.loadModules(registry);
		ItemUpgrade.loadUpgrades(registry);
		registerPipes(registry);

		registry.register(setName(new LogisticsItemCard(), "item_card"));
		registry.register(setName(new RemoteOrderer(), "remote_orderer"));
		registry.register(setName(new ItemPipeSignCreator(), "sign_creator"));
		registry.register(setName(new ItemHUDArmor(), "hud_glasses"));
		registry.register(setName(new ItemParts(), "parts"));
		registry.register(setName(new ItemBlankModule(), "module_blank"));
		registry.register(setName(new ItemDisk(), "disk"));
		registry.register(setName(new LogisticsFluidContainer(), "fluid_container"));
		registry.register(setName(new LogisticsBrokenItem(), "broken_item"));
		registry.register(setName(new ItemGuideBook(), "guide_book"));
		registry.register(setName(new ItemPipeController(), "pipe_controller"));
		registry.register(setName(new ItemPipeManager(), "pipe_manager"));
		registry.register(setName(new ItemLogisticsProgrammer(), "logistics_programmer"));
		registry.register(setName(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC), "chip_basic"));
		registry.register(setName(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_BASIC_RAW), "chip_basic_raw"));
		registry.register(setName(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED), "chip_advanced"));
		registry.register(setName(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_ADVANCED_RAW), "chip_advanced_raw"));
		registry.register(setName(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA), "chip_fpga"));
		registry.register(setName(new ItemLogisticsChips(ItemLogisticsChips.ITEM_CHIP_FPGA_RAW), "chip_fpga_raw"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.frame), "frame"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.powerJunction), "power_junction"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.securityStation), "security_station"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.crafter), "crafting_table"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.crafterFuzzy), "crafting_table_fuzzy"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.statisticsTable), "statistics_table"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.powerProviderRF), "power_provider_rf"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.powerProviderEU), "power_provider_eu"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.powerProviderMJ), "power_provider_mj"));
		registry.register(setName(new LogisticsSolidBlockItem(LPBlocks.programCompiler), "program_compiler"));
	}

	// TODO move somewhere
	public static <T extends Item> T setName(T item, String name) {
		return setName(item, name, LPConstants.LP_MOD_ID);
	}

	public static <T extends Item> T setName(T item, String name, String modID) {
		item.setRegistryName(modID, name);
		item.setUnlocalizedName(String.format("%s.%s", modID, name));
		return item;
	}

	// TODO move somewhere
	public static <T extends Block> T setName(T block, String name) {
		block.setRegistryName(LPConstants.LP_MOD_ID, name);
		block.setUnlocalizedName(String.format("%s.%s", LPConstants.LP_MOD_ID, name));
		return block;
	}

	@SubscribeEvent
	public void initBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_BLOCK_FRAME), "frame"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_POWER_JUNCTION), "power_junction"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_SECURITY_STATION), "security_station"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_AUTOCRAFTING_TABLE), "crafting_table"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_FUZZYCRAFTING_TABLE), "crafting_table_fuzzy"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_STATISTICS_TABLE), "statistics_table"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_RF_POWERPROVIDER), "power_provider_rf"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_IC2_POWERPROVIDER), "power_provider_eu"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_BC_POWERPROVIDER), "power_provider_mj"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_PROGRAM_COMPILER), "program_compiler"));

		registry.register(setName(new BlockDummy(), "solid_block"));

		registry.register(setName(new LogisticsBlockGenericPipe(), "pipe"));
		registry.register(setName(new LogisticsBlockGenericSubMultiBlock(), "sub_multiblock"));
	}

	@SubscribeEvent
	public void onModelLoad(ModelRegistryEvent e) {
		MainProxy.proxy.registerModels();
	}

	private void registerRecipes() {
		RecipeManager.recipeProvider.add(new LPChipRecipes());
		RecipeManager.recipeProvider.add(new UpgradeChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new ModuleChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new PipeChippedCraftingRecipes());
		RecipeManager.recipeProvider.add(new CraftingRecipes());
		RecipeManager.loadRecipes();

		resetRecipeList.stream()
				.map(Supplier::get)
				.forEach(itemItemPair -> registerShapelessResetRecipe(itemItemPair.getValue1(), itemItemPair.getValue2()));
	}

	@SneakyThrows
	private void loadClasses() {
		//Try to load all classes to let our checksums get generated
		forName("net.minecraft.tileentity.TileEntity");
		forName("net.minecraft.world.World");
		forName("net.minecraft.item.ItemStack");
		forName("net.minecraftforge.fluids.FluidStack");
		forName("net.minecraftforge.fluids.Fluid");
		forName("cofh.thermaldynamics.block.TileTDBase");
		forName("cofh.thermaldynamics.duct.item.TravelingItem");
		forName("crazypants.enderio.conduit.item.ItemConduit");
		forName("crazypants.enderio.conduit.item.NetworkedInventory");
		forName("crazypants.enderio.conduit.liquid.AbstractLiquidConduit");
		forName("mcmultipart.block.BlockMultipartContainer");
	}

	private void forName(String string) {
		try {
			Class.forName(string);
		} catch (Exception ignore) {}
	}

	@Mod.EventHandler
	public void beforeStart(FMLServerAboutToStartEvent event) {
		ServerTickDispatcher.INSTANCE.serverStart();
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
		ServerTickDispatcher.INSTANCE.cleanup();
		LogisticsPipes.serverConfigManager = null;
	}

	@Mod.EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new LogisticsPipesCommand());
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		if (minecraftTestStartMethod != null) minecraftTestStartMethod.accept(event);
	}

	@Mod.EventHandler
	public void certificateWarning(FMLFingerprintViolationEvent warning) {
		LogisticsPipes.certificateError = true;
		if (!LogisticsPipes.isDEBUG()) {
			System.out.println("[LogisticsPipes|Certificate] Certificate not correct");
			System.out.println("[LogisticsPipes|Certificate] Expected: " + warning.getExpectedFingerprint());
			System.out.println("[LogisticsPipes|Certificate] File: " + warning.getSource().getAbsolutePath());
			System.out.println("[LogisticsPipes|Certificate] This in not a LogisticsPipes version from RS485.");
		}
	}

	public static Object getComputerLP() {
		if (LogisticsPipes.generalAccess == null) {
			LogisticsPipes.generalAccess = new LPGlobalCCAccess();
		}
		return LogisticsPipes.generalAccess;
	}

	public void registerPipes(IForgeRegistry<Item> registry) {
		registerPipe(registry, "basic", PipeItemsBasicLogistics::new);
		registerPipe(registry, "request", PipeItemsRequestLogistics::new);
		registerPipe(registry, "provider", PipeItemsProviderLogistics::new);
		registerPipe(registry, "crafting", PipeItemsCraftingLogistics::new);
		registerPipe(registry, "satellite", PipeItemsSatelliteLogistics::new);
		registerPipe(registry, "supplier", PipeItemsSupplierLogistics::new);
		registerPipe(registry, "chassis_mk1", PipeLogisticsChassisMk1::new);
		registerPipe(registry, "chassis_mk2", PipeLogisticsChassisMk2::new);
		registerPipe(registry, "chassis_mk3", PipeLogisticsChassisMk3::new);
		registerPipe(registry, "chassis_mk4", PipeLogisticsChassisMk4::new);
		registerPipe(registry, "chassis_mk5", PipeLogisticsChassisMk5::new);
		registerPipe(registry, "request_mk2", PipeItemsRequestLogisticsMk2::new);
		registerPipe(registry, "remote_orderer", PipeItemsRemoteOrdererLogistics::new);
		registerPipe(registry, "inventory_system_connector", PipeItemsInvSysConnector::new);
		registerPipe(registry, "system_entrance", PipeItemsSystemEntranceLogistics::new);
		registerPipe(registry, "system_destination", PipeItemsSystemDestinationLogistics::new);
		registerPipe(registry, "firewall", PipeItemsFirewall::new);

		registerPipe(registry, "fluid_basic", PipeFluidBasic::new);
		registerPipe(registry, "fluid_supplier", PipeItemsFluidSupplier::new);
		registerPipe(registry, "fluid_insertion", PipeFluidInsertion::new);
		registerPipe(registry, "fluid_provider", PipeFluidProvider::new);
		registerPipe(registry, "fluid_request", PipeFluidRequestLogistics::new);
		registerPipe(registry, "fluid_extractor", PipeFluidExtractor::new);
		registerPipe(registry, "fluid_satellite", PipeFluidSatellite::new);
		registerPipe(registry, "fluid_supplier_mk2", PipeFluidSupplierMk2::new);
		registerPipe(registry, "fluid_terminus", PipeFluidTerminus::new);

		registerPipe(registry, "request_table", PipeBlockRequestTable::new);

		registerPipe(registry, "transport_basic", PipeItemsBasicTransport::new);

		registerPipe(registry, "hs_curve", HSTubeCurve::new);
		registerPipe(registry, "hs_speedup", HSTubeSpeedup::new);
		registerPipe(registry, "hs_s_curve", HSTubeSCurve::new);
		registerPipe(registry, "hs_line", HSTubeLine::new);
		registerPipe(registry, "hs_gain", HSTubeGain::new);
	}

	protected void registerPipe(IForgeRegistry<Item> registry, String name, Function<Item, ? extends CoreUnroutedPipe> constructor) {
		final ItemLogisticsPipe res = LogisticsBlockGenericPipe.registerPipe(registry, name, constructor);
		final CoreUnroutedPipe pipe = Objects.requireNonNull(LogisticsBlockGenericPipe.createPipe(res), "created a null pipe from " + res);
		if (pipe instanceof CoreRoutedPipe) {
			postInitRun.add(() -> res.setPipeIconIndex(((CoreRoutedPipe) pipe).getTextureType(null).normal, ((CoreRoutedPipe) pipe).getTextureType(null).newTexture));
		}

		if (pipe.getClass() != PipeItemsBasicLogistics.class && CoreRoutedPipe.class.isAssignableFrom(pipe.getClass())) {
			if (pipe.getClass() != PipeFluidBasic.class && FluidRoutedPipe.class.isAssignableFrom(pipe.getClass())) {
				resetRecipeList.add(() -> new Pair<>(res, LPItems.pipeFluidBasic));
			} else if (!pipe.isPipeBlock()) {
				resetRecipeList.add(() -> new Pair<>(res, LPItems.pipeBasic));
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

	public static ClientConfiguration getClientPlayerConfig() {
		if (LogisticsPipes.playerConfig == null) {
			LogisticsPipes.playerConfig = new ClientConfiguration();
		}
		return LogisticsPipes.playerConfig;
	}

	public static ServerConfigurationManager getServerConfigManager() {
		if (LogisticsPipes.serverConfigManager == null) {
			LogisticsPipes.serverConfigManager = new ServerConfigurationManager();
		}
		return LogisticsPipes.serverConfigManager;
	}
}
