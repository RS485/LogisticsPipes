/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

import net.minecraftforge.client.event.ModelRegistryEvent;
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
import logisticspipes.items.ItemGuideBook;
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
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.ProxyManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.SpecialInventoryHandlerManager;
import logisticspipes.proxy.SpecialTankHandlerManager;
import logisticspipes.proxy.computers.objects.LPGlobalCCAccess;
import logisticspipes.proxy.endercore.EnderCoreProgressProvider;
import logisticspipes.proxy.ic2.IC2ProgressProvider;
import logisticspipes.proxy.progressprovider.MachineProgressProvider;
import logisticspipes.proxy.recipeproviders.ImmibisCraftingTableMk2;
import logisticspipes.proxy.recipeproviders.LogisticsCraftingTable;
import logisticspipes.proxy.recipeproviders.RollingMachine;
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
import logisticspipes.utils.TankUtilFactory;
import logisticspipes.utils.tuples.Pair;
import network.rs485.grow.TickExecutor;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.config.ServerConfigurationManager;

//@formatter:off
//CHECKSTYLE:OFF

@Mod(
		modid = LPConstants.LP_MOD_ID,
		certificateFingerprint = "e0c86912b2f7cc0cc646ad57799574aea43dbd45",
		useMetadata = true)
public class LogisticsPipes {

	//@formatter:on
	//CHECKSTYLE:ON

	public LogisticsPipes() { //TODO: remove throws
		LaunchClassLoader loader = Launch.classLoader;
		if (!LPConstants.COREMOD_LOADED) {
			if (LPConstants.DEBUG) {
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

	@Mod.Instance("logisticspipes")
	public static LogisticsPipes instance;

	@Getter
	private static TickExecutor globalTickExecutor;

	private boolean certificateError = false;

	// other statics
	public static Textures textures = new Textures();
	public static Logger log;
	public static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public static VersionChecker versionChecker;

	// initializes the creative tab
	public static final CreativeTabs CREATIVE_TAB_LP = new CreativeTabs("Logistics_Pipes") {

		@SideOnly(Side.CLIENT)
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
		SimpleServiceLocator.setTankUtilFactory(new TankUtilFactory());
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
		LogisticsEventListener eventListener = new LogisticsEventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);
		MinecraftForge.EVENT_BUS.register(new LPChatListener());

		LPDataFixer.INSTANCE.init();

		if (event.getSide() == Side.SERVER) {
			LogisticsPipes.textures.registerBlockIcons(null);
		}
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
		LogisticsPipes.serverConfigManager = null;
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
		registerPipe(registry, "basic", PipeItemsBasicLogistics::new);
		registerPipe(registry, "request", PipeItemsRequestLogistics::new);
		registerPipe(registry, "provider", PipeItemsProviderLogistics::new);
		registerPipe(registry, "crafting", PipeItemsCraftingLogistics::new);
		registerPipe(registry, "satellite", PipeItemsSatelliteLogistics::new);
		registerPipe(registry, "supplier", PipeItemsSupplierLogistics::new);
		registerPipe(registry, "chassis_mk1", PipeLogisticsChassiMk1::new);
		registerPipe(registry, "chassis_mk2", PipeLogisticsChassiMk2::new);
		registerPipe(registry, "chassis_mk3", PipeLogisticsChassiMk3::new);
		registerPipe(registry, "chassis_mk4", PipeLogisticsChassiMk4::new);
		registerPipe(registry, "chassis_mk5", PipeLogisticsChassiMk5::new);
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
		final CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.createPipe(res);
		if (pipe instanceof CoreRoutedPipe) {
			postInitRun.add(() -> res.setPipeIconIndex(((CoreRoutedPipe) pipe).getTextureType(null).normal, ((CoreRoutedPipe) pipe).getTextureType(null).newTexture));
		}

		if (pipe.getClass() != PipeItemsBasicLogistics.class && CoreRoutedPipe.class.isAssignableFrom(pipe.getClass())) {
			if (pipe.getClass() != PipeFluidBasic.class && PipeFluidBasic.class.isAssignableFrom(pipe.getClass())) {
				resetRecipeList.add(() -> new Pair<>(res, LPItems.pipeFluidBasic));
			} else if (pipe.getClass() != PipeBlockRequestTable.class) {
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
