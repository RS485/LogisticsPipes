package logisticspipes.config;

import java.io.File;

import logisticspipes.LogisticsPipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Configs {

	public static final String CATEGORY_MULTITHREAD = "multithread";
	public static final String CATEGORY_DEBUG 		= "debug";

	// Ids
	public static int ITEM_BROKEN_ID = 6863;
	public static int ITEM_LIQUID_CONTAINER_ID = 6864;
	public static int ITEM_UPGRADE_MANAGER_ID = 6865;
	public static int ITEM_UPGRADE_ID = 6866;
	public static int ITEM_PARTS_ID = 6867;
	public static int ITEM_HUD_ID = 6868;
	public static int ITEM_CARD_ID = 6869;
	public static int ITEM_DISK_ID = 6870;
	public static int ITEM_MODULE_ID = 6871;
	public static int LOGISTICSREMOTEORDERER_ID = 6872;
	public static int LOGISTICSNETWORKMONITOR_ID = 6873;
	public static int LOGISTICSPIPE_BASIC_ID = 6874;
	public static int LOGISTICSPIPE_REQUEST_ID = 6875;
	public static int LOGISTICSPIPE_PROVIDER_ID = 6876;
	public static int LOGISTICSPIPE_CRAFTING_ID = 6877;
	public static int LOGISTICSPIPE_SATELLITE_ID = 6878;
	public static int LOGISTICSPIPE_SUPPLIER_ID = 6879;
	//Free ID from Old Builder Supplier = 6880;
	public static int LOGISTICSPIPE_CHASSI1_ID = 6881;
	public static int LOGISTICSPIPE_CHASSI2_ID = 6882;
	public static int LOGISTICSPIPE_CHASSI3_ID = 6883;
	public static int LOGISTICSPIPE_CHASSI4_ID = 6884;
	public static int LOGISTICSPIPE_CHASSI5_ID = 6885;
	public static int LOGISTICSPIPE_LIQUIDSUPPLIER_ID = 6886;
	public static int LOGISTICSPIPE_CRAFTING_MK2_ID = 6887;
	public static int LOGISTICSPIPE_REQUEST_MK2_ID = 6888;
	public static int LOGISTICSPIPE_REMOTE_ORDERER_ID = 6889;
	public static int LOGISTICSPIPE_PROVIDER_MK2_ID = 6890;
	public static int LOGISTICSPIPE_APIARIST_ANALYSER_ID = 6891;
	public static int LOGISTICSPIPE_APIARIST_SINK_ID = 6892;
	public static int LOGISTICSPIPE_INVSYSCON_ID = 6893;
	public static int LOGISTICSPIPE_ENTRANCE_ID = 6894;
	public static int LOGISTICSPIPE_DESTINATION_ID = 6895;
	public static int LOGISTICSPIPE_CRAFTING_MK3_ID = 6896;
	public static int LOGISTICSPIPE_FIREWALL_ID = 6897;
	public static int LOGISTICSPIPE_REQUEST_TABLE_ID = 6898;

	public static int LOGISTICSPIPE_LIQUID_CONNECTOR = 6901;
	public static int LOGISTICSPIPE_LIQUID_BASIC = 6902;
	public static int LOGISTICSPIPE_LIQUID_INSERTION = 6903;
	public static int LOGISTICSPIPE_LIQUID_PROVIDER = 6904;
	public static int LOGISTICSPIPE_LIQUID_REQUEST = 6905;
	public static int LOGISTICSPIPE_LIQUID_EXTRACTOR = 6906;
	public static int LOGISTICSPIPE_LIQUID_SATELLITE = 6907;
	public static int LOGISTICSPIPE_LIQUID_SUPPLIER_MK2 = 6908;

	public static int LOGISTICSCRAFTINGSIGNCREATOR_ID = 6900;

	private static Configuration CONFIGURATION;

	// Configrables
	public static int LOGISTICS_DETECTION_LENGTH = 50;
	public static int LOGISTICS_DETECTION_COUNT = 100;
	public static int LOGISTICS_DETECTION_FREQUENCY = 20;
	public static boolean LOGISTICS_ORDERER_COUNT_INVERTWHEEL = false;
	public static boolean LOGISTICS_ORDERER_PAGE_INVERTWHEEL = false;
	public static final float LOGISTICS_ROUTED_SPEED_MULTIPLIER = 20F;
	public static final float LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER = 10F;

	public static int LOGISTICS_HUD_RENDER_DISTANCE = 15;

	public static boolean LOGISTICS_POWER_USAGE_DISABLED = false;
	public static boolean LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED = false;

	public static boolean TOOLTIP_INFO = LogisticsPipes.DEBUG;
	public static boolean MANDATORY_CARPENTER_RECIPES = true;
	public static boolean ENABLE_PARTICLE_FX = true;

	// GuiOrderer Popup setting
	public static boolean DISPLAY_POPUP = true;

	// BlockID
	public static int LOGISTICS_SIGN_ID = 1100;
	public static int LOGISTICS_SOLID_BLOCK_ID = 1101;

	// MultiThread
	public static int MULTI_THREAD_NUMBER = 4;
	public static int MULTI_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	public static boolean WATCHDOG_CLIENT 	= false;
	public static boolean WATCHDOG_SERVER 	= false;
	public static int WATCHDOG_TIMEOUT		= 60000;

	public static int POWER_USAGE_MULTIPLIER = 1;
	public static int LOGISTICS_CRAFTING_TABLE_POWER_USAGE = 250;

	public static void load(FMLPreInitializationEvent event) {
		File configFile = new File(event.getModConfigurationDirectory(), "LogisticsPipes.cfg");
		CONFIGURATION = new Configuration(configFile);
		CONFIGURATION.load();

		if (CONFIGURATION.hasCategory("logisticspipe.id")
				|| CONFIGURATION.hasCategory("logisticsPipe.id")) {
			throw new RuntimeException(
					"Old config, please remove it and manually reconfigure LogisticPipes");
		}

		LOGISTICSNETWORKMONITOR_ID = CONFIGURATION.getItem(
				"logisticsNetworkMonitor.id", LOGISTICSNETWORKMONITOR_ID,
				"The item id for the network monitor").getInt();
		LOGISTICSREMOTEORDERER_ID = CONFIGURATION.getItem(
				"logisticsRemoteOrderer.id", LOGISTICSREMOTEORDERER_ID,
				"The item id for the remote orderer").getInt();
		ITEM_MODULE_ID = CONFIGURATION.getItem("logisticsModules.id",
				ITEM_MODULE_ID, "The item id for the modules").getInt();
		ITEM_UPGRADE_ID = CONFIGURATION.getItem("logisticsUpgrades.id",
				ITEM_UPGRADE_ID, "The item id for the upgrades").getInt();
		ITEM_UPGRADE_MANAGER_ID = CONFIGURATION.getItem(
				"logisticsUpgradeManager.id", ITEM_UPGRADE_MANAGER_ID,
				"The item id for the upgrade manager").getInt();
		ITEM_DISK_ID = CONFIGURATION.getItem("logisticsDisk.id", ITEM_DISK_ID,
				"The item id for the disk").getInt();
		ITEM_CARD_ID = CONFIGURATION.getItem("logisticItemCard.id",
				ITEM_CARD_ID, "The item id for the logistics item card")
				.getInt();
		ITEM_HUD_ID = CONFIGURATION.getItem("logisticsHUD.id", ITEM_HUD_ID,
				"The item id for the Logistics HUD glasses").getInt();
		ITEM_PARTS_ID = CONFIGURATION.getItem("logisticsHUDParts.id",
				ITEM_PARTS_ID, "The item id for the Logistics item parts")
				.getInt();
		ITEM_BROKEN_ID = CONFIGURATION.getItem("LogisticsBrokenItem.id",
				ITEM_BROKEN_ID, "The item id for the logistics broken item")
				.getInt();
	
		ITEM_LIQUID_CONTAINER_ID = CONFIGURATION.getItem(
				"LogisticsLiquidContainer.id", ITEM_LIQUID_CONTAINER_ID,
				"The item id for the logistics liquid container").getInt();
		
		LOGISTICSPIPE_BASIC_ID = CONFIGURATION.getItem("logisticsPipe.id",
				LOGISTICSPIPE_BASIC_ID,
				"The item id for the basic logistics pipe").getInt();
		LOGISTICSPIPE_REQUEST_ID = CONFIGURATION.getItem(
				"logisticsPipeRequester.id", LOGISTICSPIPE_REQUEST_ID,
				"The item id for the requesting logistics pipe").getInt();
		LOGISTICSPIPE_PROVIDER_ID = CONFIGURATION.getItem(
				"logisticsPipeProvider.id", LOGISTICSPIPE_PROVIDER_ID,
				"The item id for the providing logistics pipe").getInt();
		LOGISTICSPIPE_CRAFTING_ID = CONFIGURATION.getItem(
				"logisticsPipeCrafting.id", LOGISTICSPIPE_CRAFTING_ID,
				"The item id for the crafting logistics pipe").getInt();
		LOGISTICSPIPE_SATELLITE_ID = CONFIGURATION.getItem(
				"logisticsPipeSatellite.id", LOGISTICSPIPE_SATELLITE_ID,
				"The item id for the crafting satellite pipe").getInt();
		LOGISTICSPIPE_SUPPLIER_ID = CONFIGURATION.getItem(
				"logisticsPipeSupplier.id", LOGISTICSPIPE_SUPPLIER_ID,
				"The item id for the supplier pipe").getInt();
		LOGISTICSPIPE_CHASSI1_ID = CONFIGURATION.getItem(
				"logisticsPipeChassi1.id", LOGISTICSPIPE_CHASSI1_ID,
				"The item id for the chassi1").getInt();
		LOGISTICSPIPE_CHASSI2_ID = CONFIGURATION.getItem(
				"logisticsPipeChassi2.id", LOGISTICSPIPE_CHASSI2_ID,
				"The item id for the chassi2").getInt();
		LOGISTICSPIPE_CHASSI3_ID = CONFIGURATION.getItem(
				"logisticsPipeChassi3.id", LOGISTICSPIPE_CHASSI3_ID,
				"The item id for the chassi3").getInt();
		LOGISTICSPIPE_CHASSI4_ID = CONFIGURATION.getItem(
				"logisticsPipeChassi4.id", LOGISTICSPIPE_CHASSI4_ID,
				"The item id for the chassi4").getInt();
		LOGISTICSPIPE_CHASSI5_ID = CONFIGURATION.getItem(
				"logisticsPipeChassi5.id", LOGISTICSPIPE_CHASSI5_ID,
				"The item id for the chassi5").getInt();
		LOGISTICSPIPE_CRAFTING_MK2_ID = CONFIGURATION.getItem(
				"logisticsPipeCraftingMK2.id", LOGISTICSPIPE_CRAFTING_MK2_ID,
				"The item id for the crafting logistics pipe MK2").getInt();
		LOGISTICSPIPE_CRAFTING_MK3_ID = CONFIGURATION.getItem(
				"logisticsPipeCraftingMK3.id", LOGISTICSPIPE_CRAFTING_MK3_ID,
				"The item id for the crafting logistics pipe MK3").getInt();
		LOGISTICSPIPE_REQUEST_MK2_ID = CONFIGURATION.getItem(
				"logisticsPipeRequesterMK2.id", LOGISTICSPIPE_REQUEST_MK2_ID,
				"The item id for the requesting logistics pipe MK2").getInt();
		LOGISTICSPIPE_PROVIDER_MK2_ID = CONFIGURATION.getItem(
				"logisticsPipeProviderMK2.id", LOGISTICSPIPE_PROVIDER_MK2_ID,
				"The item id for the provider logistics pipe MK2").getInt();
		LOGISTICSPIPE_REMOTE_ORDERER_ID = CONFIGURATION.getItem(
				"logisticsPipeRemoteOrderer.id",
				LOGISTICSPIPE_REMOTE_ORDERER_ID,
				"The item id for the remote orderer logistics pipe").getInt();
		LOGISTICSPIPE_APIARIST_ANALYSER_ID = CONFIGURATION.getItem(
				"logisticsPipeApiaristAnalyser.id",
				LOGISTICSPIPE_APIARIST_ANALYSER_ID,
				"The item id for the apiarist logistics analyser pipe")
				.getInt();
		LOGISTICSPIPE_APIARIST_SINK_ID = CONFIGURATION.getItem(
				"logisticsPipeApiaristSink.id", LOGISTICSPIPE_APIARIST_SINK_ID,
				"The item id for the apiarist logistics sink pipe").getInt();
		LOGISTICSPIPE_ENTRANCE_ID = CONFIGURATION.getItem(
				"logisticEntrance.id", LOGISTICSPIPE_ENTRANCE_ID,
				"The item id for the logistics system entrance pipe").getInt();
		LOGISTICSPIPE_DESTINATION_ID = CONFIGURATION.getItem(
				"logisticDestination.id", LOGISTICSPIPE_DESTINATION_ID,
				"The item id for the logistics system destination pipe")
				.getInt();
		LOGISTICSPIPE_INVSYSCON_ID = CONFIGURATION.getItem(
				"logisticInvSysCon.id", LOGISTICSPIPE_INVSYSCON_ID,
				"The item id for the inventory system connector pipe").getInt();
		LOGISTICS_SIGN_ID = CONFIGURATION.getBlock("logisticsSignId",
				LOGISTICS_SIGN_ID, "The ID of the LogisticsPipes Sign")
				.getInt();
		LOGISTICS_SOLID_BLOCK_ID = CONFIGURATION.getBlock(
				"logisticsSolidBlockId", LOGISTICS_SOLID_BLOCK_ID,
				"The ID of the LogisticsPipes Solid Block").getInt();
		LOGISTICSPIPE_FIREWALL_ID = CONFIGURATION.getItem(
				"logisticsPipeFirewall.id", LOGISTICSPIPE_FIREWALL_ID,
				"The item id for the firewall logistics pipe").getInt();

		LOGISTICSPIPE_LIQUID_CONNECTOR = CONFIGURATION.getItem(
				"logisticPipeLiquidConnector.id",
				LOGISTICSPIPE_LIQUID_CONNECTOR,
				"The item id for the liquid connector pipe.").getInt();
		LOGISTICSPIPE_LIQUID_BASIC = CONFIGURATION.getItem(
				"logisticPipeLiquidBasic.id", LOGISTICSPIPE_LIQUID_BASIC,
				"The item id for the liquid basic pipe.").getInt();
		LOGISTICSPIPE_LIQUID_INSERTION = CONFIGURATION.getItem(
				"logisticPipeLiquidInsertion.id",
				LOGISTICSPIPE_LIQUID_INSERTION,
				"The item id for the liquid insertion pipe.").getInt();
		LOGISTICSPIPE_LIQUID_PROVIDER = CONFIGURATION.getItem(
				"logisticPipeLiquidProvider.id",
				LOGISTICSPIPE_LIQUID_PROVIDER,
				"The item id for the liquid provider pipe.").getInt();
		LOGISTICSPIPE_LIQUID_REQUEST = CONFIGURATION.getItem(
				"logisticPipeLiquidRequest.id",
				LOGISTICSPIPE_LIQUID_REQUEST,
				"The item id for the liquid requestor pipe.").getInt();
		LOGISTICSPIPE_LIQUID_EXTRACTOR = CONFIGURATION.getItem(
				"logisticPipeLiquidExtractor.id",
				LOGISTICSPIPE_LIQUID_EXTRACTOR,
				"The item id for the liquid extractor pipe.").getInt();
		LOGISTICSPIPE_LIQUID_SATELLITE = CONFIGURATION.getItem(
				"logisticPipeLiquidSatellite.id",
				LOGISTICSPIPE_LIQUID_SATELLITE,
				"The item id for the liquid satellite pipe.").getInt();
		LOGISTICSPIPE_LIQUID_SUPPLIER_MK2 = CONFIGURATION.getItem(
				"logisticPipeLiquidSupplierMk2.id",
				LOGISTICSPIPE_LIQUID_SUPPLIER_MK2,
				"The item id for the liquid supplier pipe mk2.").getInt();
		
		LOGISTICS_DETECTION_LENGTH = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"detectionLength",
						LOGISTICS_DETECTION_LENGTH,
						"The maximum shortest length between logistics pipes. This is an indicator on the maxim depth of the recursion algorithm to discover logistics neighbours. A low value might use less CPU, a high value will allow longer pipe sections")
				.getInt();
		LOGISTICS_DETECTION_COUNT = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"detectionCount",
						LOGISTICS_DETECTION_COUNT,
						"The maximum number of buildcraft pipes (including forks) between logistics pipes. This is an indicator of the maximum amount of nodes the recursion algorithm will visit before giving up. As it is possible to fork a pipe connection using standard BC pipes the algorithm will attempt to discover all available destinations through that pipe. Do note that the logistics system will not interfere with the operation of non-logistics pipes. So a forked pipe will usually be sup-optimal, but it is possible. A low value might reduce CPU usage, a high value will be able to handle more complex pipe setups. If you never fork your connection between the logistics pipes this has the same meaning as detectionLength and the lower of the two will be used")
				.getInt();
		LOGISTICS_DETECTION_FREQUENCY = Math
				.max(CONFIGURATION
						.get(Configuration.CATEGORY_GENERAL,
								"detectionFrequency",
								LOGISTICS_DETECTION_FREQUENCY,
								"The amount of time that passes between checks to see if it is still connected to its neighbours. A low value will mean that it will detect changes faster but use more CPU. A high value means detection takes longer, but CPU consumption is reduced. A value of 20 will check about every second")
						.getInt(), 1);
		LOGISTICS_ORDERER_COUNT_INVERTWHEEL = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL, "ordererCountInvertWheel",
						LOGISTICS_ORDERER_COUNT_INVERTWHEEL,
						"Inverts the the mouse wheel scrolling for remote order number of items")
				.getBoolean(false);
		LOGISTICS_ORDERER_PAGE_INVERTWHEEL = CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "ordererPageInvertWheel",
				LOGISTICS_ORDERER_PAGE_INVERTWHEEL,
				"Inverts the the mouse wheel scrolling for remote order pages")
				.getBoolean(false);

		LOGISTICS_POWER_USAGE_DISABLED = CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "powerUsageDisabled",
				LOGISTICS_POWER_USAGE_DISABLED,
				"Disable the power usage trough LogisticsPipes").getBoolean(
				false);
		LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"TileReplaceDisabled",
						LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED,
						"Diable the Replacement of the TileGenericPipe trough the LogisticsTileGenericPipe")
				.getBoolean(false);

		LOGISTICSCRAFTINGSIGNCREATOR_ID = CONFIGURATION.getItem(
				"logisticsCraftingSignCreator.id",
				LOGISTICSCRAFTINGSIGNCREATOR_ID,
				"The item id for the crafting sign creator").getInt();

		LOGISTICS_HUD_RENDER_DISTANCE = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL, "HUDRenderDistance",
						LOGISTICS_HUD_RENDER_DISTANCE,
						"The max. distance between a player and the HUD that get's shown in blocks.")
				.getInt();

		DISPLAY_POPUP = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"displayPopup",
						DISPLAY_POPUP,
						"Set the default configuration for the popup of the Orderer Gui. Should it be used?")
				.getBoolean(false);

		LOGISTICSPIPE_LIQUIDSUPPLIER_ID = CONFIGURATION.getItem(
				"logisticsPipeLiquidSupplier.id",
				LOGISTICSPIPE_LIQUIDSUPPLIER_ID,
				"The item id for the liquid supplier pipe").getInt();
		MANDATORY_CARPENTER_RECIPES = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"mandatoryCarpenterRecipes",
						MANDATORY_CARPENTER_RECIPES,
						"Whether or not the Carpenter is required to craft Forestry related pipes/modules.")
				.getBoolean(false);
		ENABLE_PARTICLE_FX = CONFIGURATION.get(Configuration.CATEGORY_GENERAL,
				"enableParticleFX", ENABLE_PARTICLE_FX,
				"Whether or not special particles will spawn.").getBoolean(
				false);

		if(CONFIGURATION.hasKey(CATEGORY_MULTITHREAD, "enabled")) {
			//ConfigCategory.remove is deprecated, but there's no other way to remove a key-value pair without completely recreating the config...
			CONFIGURATION.getCategory(CATEGORY_MULTITHREAD).remove(new String("enabled"));
		}
		MULTI_THREAD_NUMBER = CONFIGURATION.get(CATEGORY_MULTITHREAD, "count",
				MULTI_THREAD_NUMBER, "Number of routing table update Threads, 0 to disable.").getInt();
		if (MULTI_THREAD_NUMBER < 0) {
			MULTI_THREAD_NUMBER = 0;
			CONFIGURATION.get(CATEGORY_MULTITHREAD, "count",
					MULTI_THREAD_NUMBER, "Number of routing table update Threads, 0 to disable.").set(Integer
					.toString(MULTI_THREAD_NUMBER));
		}
		MULTI_THREAD_PRIORITY = CONFIGURATION
				.get(CATEGORY_MULTITHREAD, "priority", MULTI_THREAD_PRIORITY,
						"Priority of the multiThread Threads. 10 is highest, 5 normal, 1 lowest")
				.getInt();
		if (MULTI_THREAD_PRIORITY < 1 || MULTI_THREAD_PRIORITY > 10) {
			MULTI_THREAD_PRIORITY = Thread.NORM_PRIORITY;
			CONFIGURATION
					.get(CATEGORY_MULTITHREAD, "priority",
							MULTI_THREAD_PRIORITY,
							"Priority of the multiThread Threads. 10 is highest, 5 normal, 1 lowest").set(Integer
					.toString(Thread.NORM_PRIORITY));
		}

		WATCHDOG_CLIENT = CONFIGURATION
				.get(CATEGORY_DEBUG, "watchdog_client", WATCHDOG_CLIENT,
						"Enable the LP thread watchdog client side.").getBoolean(false);
		WATCHDOG_SERVER = CONFIGURATION
				.get(CATEGORY_DEBUG, "watchdog_server", WATCHDOG_SERVER,
						"Enable the LP thread watchdog server side.").getBoolean(false);
		WATCHDOG_TIMEOUT = CONFIGURATION
				.get(CATEGORY_DEBUG, "watchdog_timeout", WATCHDOG_TIMEOUT,
						"The LP thread watchdog timeout time in ms.").getInt();

		
		POWER_USAGE_MULTIPLIER = CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "powerUsageMultiplyer",
				POWER_USAGE_MULTIPLIER, "A Multiplyer for the power usage.")
				.getInt();

		if (POWER_USAGE_MULTIPLIER < 1) {
			POWER_USAGE_MULTIPLIER = 1;
			CONFIGURATION.get(Configuration.CATEGORY_GENERAL,
					"powerUsageMultiplyer", POWER_USAGE_MULTIPLIER,
					"A Multiplyer for the power usage.").set("1");
		}

		LOGISTICS_CRAFTING_TABLE_POWER_USAGE = Math
				.max(CONFIGURATION
						.get(Configuration.CATEGORY_GENERAL,
								"logisticsCraftingTablePowerUsage",
								LOGISTICS_CRAFTING_TABLE_POWER_USAGE,
								"Number of LPower units the Logistics Crafting Table uses per craft.")
						.getInt(), 0);

		LOGISTICSPIPE_REQUEST_TABLE_ID = CONFIGURATION.getItem(
				"logisticsPipeRequestTable.id", LOGISTICSPIPE_REQUEST_TABLE_ID,
				"The item id for the request table").getInt();

		CONFIGURATION.save();
	}

	public static void savePopupState() {
		Property pageDisplayPopupProperty = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"displayPopup",
						DISPLAY_POPUP,
						"Set the default configuration for the popup of the Orderer Gui. Should it be used?");
		pageDisplayPopupProperty.set(Boolean.toString(DISPLAY_POPUP));
		CONFIGURATION.save();
	}
}
