/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import org.apache.logging.log4j.Logger;

import logisticspipes.blocks.BlockDummy;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.logistics.LogisticsFluidManagerImpl;
import logisticspipes.logistics.LogisticsManagerImpl;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericSubMultiBlock;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.ProxyManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.recipes.CraftingRecipes;
import logisticspipes.recipes.LPChipRecipes;
import logisticspipes.recipes.ModuleChippedCraftingRecipes;
import logisticspipes.recipes.PipeChippedCraftingRecipes;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.UpgradeChippedCraftingRecipes;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.RouterManagerImpl;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.channels.ChannelManagerProviderImpl;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.ticks.HudUpdateTick;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.StaticResolverUtil;
import logisticspipes.utils.TankUtilFactory;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.grow.TickExecutor;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.config.LPConfiguration;
import network.rs485.logisticspipes.config.ServerConfigurationManager;

public class LogisticsPipes {

	private boolean certificateError = false;

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

	private static ClientConfiguration playerConfig;
	private static ServerConfigurationManager serverConfigManager;

	private List<Supplier<Tuple2<Item, Item>>> resetRecipeList = new ArrayList<>();

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		registerRecipes(); // TODO data fileS!!!!!

		//Register Network channels
		MainProxy.createChannels();

		for (int i = 0; i < LPConfiguration.INSTANCE.getThreads(); i++) {
			new RoutingTableUpdateThread(i);
		}
		LogisticsEventListener eventListener = new LogisticsEventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		StaticResolverUtil.useASMDataTable(evt.getAsmData());
		PacketHandler.initialize();
		NewGuiHandler.initialize();

		LogisticsPipes.log = evt.getModLog();
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

		SimpleServiceLocator.pipeInformationManager = new PipeInformationManager();
		SimpleServiceLocator.logisticsFluidManager = new LogisticsFluidManagerImpl();

		MainProxy.proxy.initModelLoader();
	}

	@SubscribeEvent
	public void initBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_BLOCK_FRAME, settings), "frame"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_POWER_JUNCTION, settings), "power_junction"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_SECURITY_STATION, settings), "security_station"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_AUTOCRAFTING_TABLE, settings), "crafting_table"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_FUZZYCRAFTING_TABLE, settings), "crafting_table_fuzzy"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_STATISTICS_TABLE, settings), "statistics_table"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_RF_POWERPROVIDER, settings), "power_provider_rf"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_IC2_POWERPROVIDER, settings), "power_provider_eu"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_BC_POWERPROVIDER, settings), "power_provider_mj"));
		registry.register(setName(new LogisticsSolidBlock(LogisticsSolidBlock.Type.LOGISTICS_PROGRAM_COMPILER, settings), "program_compiler"));

		registry.register(setName(new BlockDummy(), "solid_block"));

		registry.register(setName(new LogisticsBlockGenericPipe(), "pipe"));
		registry.register(setName(new LogisticsBlockGenericSubMultiBlock(), "sub_multiblock"));
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
		LogisticsPipes.serverConfigManager = null;
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
