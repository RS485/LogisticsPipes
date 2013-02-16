package logisticspipes.config;

import java.io.File;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Configs {
	
	public static final String CATEGORY_MULTITHREAD = "multiThread";
	
	// Ids
	public static int ItemLiquidContainerId							= 6864;
	public static int ItemUpgradeManagerId							= 6865;
	public static int ItemUpgradeId									= 6866;
	public static int ItemPartsId									= 6867;
	public static int ItemHUDId										= 6868;
	public static int ItemCardId									= 6869;
	public static int ItemDiskId									= 6870;
	public static int ItemModuleId									= 6871;
	public static int LOGISTICSREMOTEORDERER_ID						= 6872;
	public static int LOGISTICSNETWORKMONITOR_ID					= 6873;
	public static int LOGISTICSPIPE_BASIC_ID						= 6874;
	public static int LOGISTICSPIPE_REQUEST_ID						= 6875;
	public static int LOGISTICSPIPE_PROVIDER_ID						= 6876;
	public static int LOGISTICSPIPE_CRAFTING_ID						= 6877;
	public static int LOGISTICSPIPE_SATELLITE_ID					= 6878;
	public static int LOGISTICSPIPE_SUPPLIER_ID						= 6879;
	public static int LOGISTICSPIPE_BUILDERSUPPLIER_ID				= 6880;
	public static int LOGISTICSPIPE_CHASSI1_ID						= 6881;
	public static int LOGISTICSPIPE_CHASSI2_ID						= 6882;
	public static int LOGISTICSPIPE_CHASSI3_ID						= 6883;
	public static int LOGISTICSPIPE_CHASSI4_ID						= 6884;
	public static int LOGISTICSPIPE_CHASSI5_ID						= 6885;
	public static int LOGISTICSPIPE_LIQUIDSUPPLIER_ID				= 6886;
	public static int LOGISTICSPIPE_CRAFTING_MK2_ID					= 6887;
	public static int LOGISTICSPIPE_REQUEST_MK2_ID					= 6888;
	public static int LOGISTICSPIPE_REMOTE_ORDERER_ID				= 6889;
	public static int LOGISTICSPIPE_PROVIDER_MK2_ID					= 6890;
	public static int LOGISTICSPIPE_APIARIST_ANALYSER_ID			= 6891;
	public static int LOGISTICSPIPE_APIARIST_SINK_ID				= 6892;
	public static int LOGISTICSPIPE_INVSYSCON_ID					= 6893;
	public static int LOGISTICSPIPE_ENTRANCE_ID						= 6894;
	public static int LOGISTICSPIPE_DESTINATION_ID					= 6895;
	public static int LOGISTICSPIPE_CRAFTING_MK3_ID					= 6896;
	public static int LOGISTICSPIPE_FIREWALL_ID						= 6897;

	public static int LOGISTICSPIPE_LIQUID_CONNECTOR				= 6901;
	public static int LOGISTICSPIPE_LIQUID_BASIC					= 6902;
	public static int LOGISTICSPIPE_LIQUID_INSERTION				= 6903;
	public static int LOGISTICSPIPE_LIQUID_PROVIDER					= 6904;
	public static int LOGISTICSPIPE_LIQUID_REQUEST					= 6905;
	
	public static int LOGISTICSCRAFTINGSIGNCREATOR_ID				= 6900;
	
	private static Configuration configuration;
	
	// Configrables
	public static int LOGISTICS_DETECTION_LENGTH	= 50;
	public static int LOGISTICS_DETECTION_COUNT		= 100;
	public static int LOGISTICS_DETECTION_FREQUENCY = 20;
	public static boolean LOGISTICS_ORDERER_COUNT_INVERTWHEEL = false;
	public static boolean LOGISTICS_ORDERER_PAGE_INVERTWHEEL = false;
	public static final float LOGISTICS_ROUTED_SPEED_MULTIPLIER	= 20F;
	public static final float LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER = 10F;
	
	public static int LOGISTICS_HUD_RENDER_DISTANCE = 15;
	
	public static boolean LOGISTICS_POWER_USAGE_DISABLED = false;
	public static boolean LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED = false;
	
	public static boolean ToolTipInfo = LogisticsPipes.DEBUG;
	public static boolean MANDATORY_CARPENTER_RECIPES = true;
	public static boolean ENABLE_PARTICLE_FX = true;

	//GuiOrderer Popup setting
	public static boolean displayPopup = true;
	
	//BlockID
	public static int LOGISTICS_SIGN_ID = 1100;
	public static int LOGISTICS_SOLID_BLOCK_ID = 1101;
	
	//MultiThread
	public static boolean multiThreadEnabled = false;
	public static int multiThreadNumber = 4;
	public static int multiThreadPriority = Thread.NORM_PRIORITY;
	
	public static int powerUsageMultiplyer = 1;


	private static void readoldconfig() {
		Property logisticRemoteOrdererIdProperty = configuration.get("logisticsRemoteOrderer.id", Configuration.CATEGORY_ITEM, LOGISTICSREMOTEORDERER_ID);
		Property logisticNetworkMonitorIdProperty = configuration.get("logisticsNetworkMonitor.id", Configuration.CATEGORY_ITEM, LOGISTICSNETWORKMONITOR_ID);
		Property logisticPipeIdProperty = configuration.get("logisticsPipe.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_BASIC_ID);
		Property logisticPipeRequesterIdProperty = configuration.get("logisticsPipeRequester.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REQUEST_ID);
		Property logisticPipeProviderIdProperty = configuration.get("logisticsPipeProvider.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_PROVIDER_ID);
		Property logisticPipeCraftingIdProperty = configuration.get("logisticsPipeCrafting.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_ID);
		Property logisticPipeSatelliteIdProperty = configuration.get("logisticsPipeSatellite.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_SATELLITE_ID);
		Property logisticPipeSupplierIdProperty = configuration.get("logisticsPipeSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_SUPPLIER_ID);
		Property logisticPipeChassi1IdProperty = configuration.get("logisticsPipeChassi1.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI1_ID);
		Property logisticPipeChassi2IdProperty = configuration.get("logisticsPipeChassi2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI2_ID);
		Property logisticPipeChassi3IdProperty = configuration.get("logisticsPipeChassi3.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI3_ID);
		Property logisticPipeChassi4IdProperty = configuration.get("logisticsPipeChassi4.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI4_ID);
		Property logisticPipeChassi5IdProperty = configuration.get("logisticsPipeChassi5.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI5_ID);
		Property logisticPipeCraftingMK2IdProperty = configuration.get("logisticsPipeCraftingMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_MK2_ID);
		Property logisticPipeCraftingMK3IdProperty = configuration.get("logisticsPipeCraftingMK3.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_MK3_ID);
		Property logisticPipeRequesterMK2IdProperty = configuration.get("logisticsPipeRequesterMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REQUEST_MK2_ID);
		Property logisticPipeProviderMK2IdProperty = configuration.get("logisticsPipeProviderMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_PROVIDER_MK2_ID);
		Property logisticPipeApiaristAnalyserIdProperty = configuration.get("logisticsPipeApiaristAnalyser.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_APIARIST_ANALYSER_ID);
		Property logisticPipeRemoteOrdererIdProperty = configuration.get("logisticsPipeRemoteOrderer.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REMOTE_ORDERER_ID);
		Property logisticPipeApiaristSinkIdProperty = configuration.get("logisticsPipeApiaristSink.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_APIARIST_SINK_ID);
		Property logisticModuleIdProperty = configuration.get("logisticsModules.id", Configuration.CATEGORY_ITEM, ItemModuleId);
		Property logisticItemDiskIdProperty = configuration.get("logisticsDisk.id", Configuration.CATEGORY_ITEM, ItemDiskId);
		Property logisticItemHUDIdProperty = configuration.get("logisticsHUD.id", Configuration.CATEGORY_ITEM, ItemHUDId);
		Property logisticItemPartsIdProperty = configuration.get("logisticsHUDParts.id", Configuration.CATEGORY_ITEM, ItemPartsId);
		Property logisticCraftingSignCreatorIdProperty = configuration.get("logisticsCraftingSignCreator.id", Configuration.CATEGORY_ITEM, LOGISTICSCRAFTINGSIGNCREATOR_ID);
		Property logisticPipeBuilderSupplierIdProperty = configuration.get("logisticsPipeBuilderSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_BUILDERSUPPLIER_ID);
		Property logisticPipeLiquidSupplierIdProperty = configuration.get("logisticsPipeLiquidSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_LIQUIDSUPPLIER_ID);
		Property logisticInvSysConIdProperty = configuration.get("logisticInvSysCon.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_INVSYSCON_ID);
		Property logisticEntranceIdProperty = configuration.get("logisticEntrance.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_ENTRANCE_ID);
		Property logisticDestinationIdProperty = configuration.get("logisticDestination.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_DESTINATION_ID);
		Property logisticItemCardIdProperty = configuration.get("logisticItemCard.id", Configuration.CATEGORY_ITEM, ItemCardId);
		Property detectionLength = configuration.get("detectionLength", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_LENGTH);
		Property detectionCount = configuration.get("detectionCount", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_COUNT);
		Property detectionFrequency = configuration.get("detectionFrequency", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_FREQUENCY);
		Property countInvertWheelProperty = configuration.get("ordererCountInvertWheel", Configuration.CATEGORY_GENERAL, LOGISTICS_ORDERER_COUNT_INVERTWHEEL);
		Property pageInvertWheelProperty = configuration.get("ordererPageInvertWheel", Configuration.CATEGORY_GENERAL, LOGISTICS_ORDERER_PAGE_INVERTWHEEL);
		Property pageDisplayPopupProperty = configuration.get("displayPopup", Configuration.CATEGORY_GENERAL, displayPopup);
		if(configuration.categories.containsKey(Configuration.CATEGORY_BLOCK) && configuration.categories.get(Configuration.CATEGORY_BLOCK).containsKey("logisticsBlockId")) {
			Property logisticsBlockId = configuration.get("logisticsBlockId", Configuration.CATEGORY_BLOCK, LOGISTICS_SIGN_ID);
			LOGISTICS_SIGN_ID = Integer.parseInt(logisticsBlockId.value);
			configuration.categories.get(Configuration.CATEGORY_BLOCK).remove("logisticsBlockId");
		}
		Property logisticsSignId = configuration.get("logisticsSignId", Configuration.CATEGORY_BLOCK, LOGISTICS_SIGN_ID);
		Property logisticsSolidBlockId = configuration.get("logisticsSolidBlockId", Configuration.CATEGORY_BLOCK, LOGISTICS_SOLID_BLOCK_ID);
		Property logisticsPowerUsageDisable = configuration.get("powerUsageDisabled", Configuration.CATEGORY_GENERAL, LOGISTICS_POWER_USAGE_DISABLED);
		Property logisticsTileGenericReplacementDisable = configuration.get("TileReplaceDisabled", Configuration.CATEGORY_GENERAL, LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED);
		Property logisticsHUDRenderDistance = configuration.get("HUDRenderDistance", Configuration.CATEGORY_GENERAL, LOGISTICS_HUD_RENDER_DISTANCE);
		Property mandatoryCarpenterRecipies = configuration.get("mandatoryCarpenterRecipies", Configuration.CATEGORY_GENERAL, MANDATORY_CARPENTER_RECIPES);
		Property enableParticleFX = configuration.get("enableParticleFX", Configuration.CATEGORY_GENERAL, ENABLE_PARTICLE_FX);
		
		LOGISTICSNETWORKMONITOR_ID			= Integer.parseInt(logisticNetworkMonitorIdProperty.value);
		LOGISTICSREMOTEORDERER_ID			= Integer.parseInt(logisticRemoteOrdererIdProperty.value);
		ItemModuleId						= Integer.parseInt(logisticModuleIdProperty.value);
		ItemDiskId							= Integer.parseInt(logisticItemDiskIdProperty.value);
		ItemCardId							= Integer.parseInt(logisticItemCardIdProperty.value);
		ItemHUDId							= Integer.parseInt(logisticItemHUDIdProperty.value);
		ItemPartsId							= Integer.parseInt(logisticItemPartsIdProperty.value);
		 
		LOGISTICSPIPE_BASIC_ID 				= Integer.parseInt(logisticPipeIdProperty.value);
		LOGISTICSPIPE_REQUEST_ID			= Integer.parseInt(logisticPipeRequesterIdProperty.value);
		LOGISTICSPIPE_PROVIDER_ID			= Integer.parseInt(logisticPipeProviderIdProperty.value);
		LOGISTICSPIPE_CRAFTING_ID			= Integer.parseInt(logisticPipeCraftingIdProperty.value);
		LOGISTICSPIPE_SATELLITE_ID			= Integer.parseInt(logisticPipeSatelliteIdProperty.value);
		LOGISTICSPIPE_SUPPLIER_ID			= Integer.parseInt(logisticPipeSupplierIdProperty.value);
		LOGISTICSPIPE_CHASSI1_ID			= Integer.parseInt(logisticPipeChassi1IdProperty.value);
		LOGISTICSPIPE_CHASSI2_ID			= Integer.parseInt(logisticPipeChassi2IdProperty.value);
		LOGISTICSPIPE_CHASSI3_ID			= Integer.parseInt(logisticPipeChassi3IdProperty.value);
		LOGISTICSPIPE_CHASSI4_ID			= Integer.parseInt(logisticPipeChassi4IdProperty.value);
		LOGISTICSPIPE_CHASSI5_ID			= Integer.parseInt(logisticPipeChassi5IdProperty.value);
		LOGISTICSPIPE_CRAFTING_MK2_ID		= Integer.parseInt(logisticPipeCraftingMK2IdProperty.value);
		LOGISTICSPIPE_CRAFTING_MK3_ID       = Integer.parseInt(logisticPipeCraftingMK3IdProperty.value);
		LOGISTICSPIPE_REQUEST_MK2_ID		= Integer.parseInt(logisticPipeRequesterMK2IdProperty.value);
		LOGISTICSPIPE_PROVIDER_MK2_ID		= Integer.parseInt(logisticPipeProviderMK2IdProperty.value);
		LOGISTICSPIPE_REMOTE_ORDERER_ID		= Integer.parseInt(logisticPipeRemoteOrdererIdProperty.value);
		LOGISTICSPIPE_APIARIST_ANALYSER_ID	= Integer.parseInt(logisticPipeApiaristAnalyserIdProperty.value);
		LOGISTICSPIPE_APIARIST_SINK_ID		= Integer.parseInt(logisticPipeApiaristSinkIdProperty.value);
		LOGISTICSPIPE_ENTRANCE_ID			= Integer.parseInt(logisticEntranceIdProperty.value);
		LOGISTICSPIPE_DESTINATION_ID		= Integer.parseInt(logisticDestinationIdProperty.value);
		LOGISTICSPIPE_INVSYSCON_ID			= Integer.parseInt(logisticInvSysConIdProperty.value);
		LOGISTICS_SIGN_ID 					= Integer.parseInt(logisticsSignId.value);
		LOGISTICS_SOLID_BLOCK_ID 			= Integer.parseInt(logisticsSolidBlockId.value);
		
		LOGISTICS_DETECTION_LENGTH			= Integer.parseInt(detectionLength.value);
		LOGISTICS_DETECTION_COUNT			= Integer.parseInt(detectionCount.value);
		LOGISTICS_DETECTION_FREQUENCY 		= Math.max(Integer.parseInt(detectionFrequency.value), 1);
		LOGISTICS_ORDERER_COUNT_INVERTWHEEL = Boolean.parseBoolean(countInvertWheelProperty.value);
		LOGISTICS_ORDERER_PAGE_INVERTWHEEL 	= Boolean.parseBoolean(pageInvertWheelProperty.value);
		
		LOGISTICS_POWER_USAGE_DISABLED	 	= Boolean.parseBoolean(logisticsPowerUsageDisable.value);
		LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED = Boolean.parseBoolean(logisticsTileGenericReplacementDisable.value);
		
		LOGISTICSCRAFTINGSIGNCREATOR_ID		= Integer.parseInt(logisticCraftingSignCreatorIdProperty.value);

		LOGISTICS_HUD_RENDER_DISTANCE 		= Integer.parseInt(logisticsHUDRenderDistance.value);
		
		displayPopup 						= Boolean.parseBoolean(pageDisplayPopupProperty.value);

		LOGISTICSPIPE_BUILDERSUPPLIER_ID	= Integer.parseInt(logisticPipeBuilderSupplierIdProperty.value);
		LOGISTICSPIPE_LIQUIDSUPPLIER_ID		= Integer.parseInt(logisticPipeLiquidSupplierIdProperty.value);
		MANDATORY_CARPENTER_RECIPES			= Boolean.parseBoolean(mandatoryCarpenterRecipies.value);
		ENABLE_PARTICLE_FX 					= Boolean.parseBoolean(enableParticleFX.value);
	}
	
	public static void load() {
		File configFile = null;
		if(MainProxy.isClient()) {
			configFile = new File(net.minecraft.client.Minecraft.getMinecraftDir(), "config/LogisticsPipes.cfg");
		} else if(MainProxy.isServer()) {
			configFile = net.minecraft.server.MinecraftServer.getServer().getFile("config/LogisticsPipes.cfg");
		} else {
			ModLoader.getLogger().severe("No server, no client? Where am I running?");
			return;
		}
		configuration = new Configuration(configFile);
		configuration.load();
		
		if(configuration.hasCategory("logisticspipe.id") || configuration.hasCategory("logisticsPipe.id")) {
			readoldconfig();
			configuration.categories.clear();
		}
		
		Property logisticRemoteOrdererIdProperty = configuration.getItem("logisticsRemoteOrderer.id", LOGISTICSREMOTEORDERER_ID);
		logisticRemoteOrdererIdProperty.comment = "The item id for the remote orderer";
		
		Property logisticNetworkMonitorIdProperty = configuration.getItem("logisticsNetworkMonitor.id", LOGISTICSNETWORKMONITOR_ID);
		logisticNetworkMonitorIdProperty.comment = "The item id for the network monitor";
		
		Property logisticPipeIdProperty = configuration.getItem("logisticsPipe.id", LOGISTICSPIPE_BASIC_ID);
		logisticPipeIdProperty.comment = "The item id for the basic logistics pipe";
		
		Property logisticPipeRequesterIdProperty = configuration.getItem("logisticsPipeRequester.id", LOGISTICSPIPE_REQUEST_ID);
		logisticPipeRequesterIdProperty.comment = "The item id for the requesting logistics pipe";
		
		Property logisticPipeProviderIdProperty = configuration.getItem("logisticsPipeProvider.id", LOGISTICSPIPE_PROVIDER_ID);
		logisticPipeProviderIdProperty.comment = "The item id for the providing logistics pipe";
		
		Property logisticPipeCraftingIdProperty = configuration.getItem("logisticsPipeCrafting.id", LOGISTICSPIPE_CRAFTING_ID);
		logisticPipeCraftingIdProperty.comment = "The item id for the crafting logistics pipe";

		Property logisticPipeSatelliteIdProperty = configuration.getItem("logisticsPipeSatellite.id", LOGISTICSPIPE_SATELLITE_ID);
		logisticPipeSatelliteIdProperty.comment = "The item id for the crafting satellite pipe";
		
		Property logisticPipeSupplierIdProperty = configuration.getItem("logisticsPipeSupplier.id", LOGISTICSPIPE_SUPPLIER_ID);
		logisticPipeSupplierIdProperty.comment = "The item id for the supplier pipe";
		
		Property logisticPipeChassi1IdProperty = configuration.getItem("logisticsPipeChassi1.id", LOGISTICSPIPE_CHASSI1_ID);
		logisticPipeChassi1IdProperty.comment = "The item id for the chassi1";
		
		Property logisticPipeChassi2IdProperty = configuration.getItem("logisticsPipeChassi2.id", LOGISTICSPIPE_CHASSI2_ID);
		logisticPipeChassi2IdProperty.comment = "The item id for the chassi2";
		
		Property logisticPipeChassi3IdProperty = configuration.getItem("logisticsPipeChassi3.id", LOGISTICSPIPE_CHASSI3_ID);
		logisticPipeChassi3IdProperty.comment = "The item id for the chassi3";
		
		Property logisticPipeChassi4IdProperty = configuration.getItem("logisticsPipeChassi4.id", LOGISTICSPIPE_CHASSI4_ID);
		logisticPipeChassi4IdProperty.comment = "The item id for the chassi4";
		
		Property logisticPipeChassi5IdProperty = configuration.getItem("logisticsPipeChassi5.id", LOGISTICSPIPE_CHASSI5_ID);
		logisticPipeChassi5IdProperty.comment = "The item id for the chassi5";

		Property logisticPipeCraftingMK2IdProperty = configuration.getItem("logisticsPipeCraftingMK2.id", LOGISTICSPIPE_CRAFTING_MK2_ID);
		logisticPipeCraftingMK2IdProperty.comment = "The item id for the crafting logistics pipe MK2";
		
		Property logisticPipeCraftingMK3IdProperty = configuration.getItem("logisticsPipeCraftingMK3.id", LOGISTICSPIPE_CRAFTING_MK3_ID);
		logisticPipeCraftingMK3IdProperty.comment = "The item id for the crafting logistics pipe MK3";

		Property logisticPipeFirewallIdProperty = configuration.getItem("logisticsPipeFirewall.id", LOGISTICSPIPE_FIREWALL_ID);
		logisticPipeFirewallIdProperty.comment = "The item id for the firewall logistics pipe";
		
		//DEBUG (TEST) ONLY (LIQUID)
		Property logisticPipeLiquidConnectorIdProperty = null;
		Property logisticPipeLiquidBasicIdProperty = null;
		Property logisticPipeLiquidInsertionIdProperty = null;
		Property logisticPipeLiquidProviderIdProperty = null;
		Property logisticPipeLiquidRequestIdProperty = null;
		if(LogisticsPipes.DEBUG) {
			
			logisticPipeLiquidConnectorIdProperty = configuration.getItem("logisticPipeLiquidConnector.id", LOGISTICSPIPE_LIQUID_CONNECTOR);
			logisticPipeLiquidConnectorIdProperty.comment = "The item id for the liquid connector pipe.";
			
			logisticPipeLiquidBasicIdProperty = configuration.getItem("logisticPipeLiquidBasic.id", LOGISTICSPIPE_LIQUID_BASIC);
			logisticPipeLiquidBasicIdProperty.comment = "The item id for the liquid basic pipe.";
			
			logisticPipeLiquidInsertionIdProperty = configuration.getItem("logisticPipeLiquidInsertion.id", LOGISTICSPIPE_LIQUID_INSERTION);
			logisticPipeLiquidInsertionIdProperty.comment = "The item id for the liquid insertion pipe.";
			
			logisticPipeLiquidProviderIdProperty = configuration.getItem("logisticPipeLiquidProvider.id", LOGISTICSPIPE_LIQUID_PROVIDER);
			logisticPipeLiquidProviderIdProperty.comment = "The item id for the liquid provider pipe.";
			
			logisticPipeLiquidRequestIdProperty = configuration.getItem("logisticPipeLiquidRequest.id", LOGISTICSPIPE_LIQUID_REQUEST);
			logisticPipeLiquidRequestIdProperty.comment = "The item id for the liquid requestor pipe.";
		}
		
		Property logisticPipeRequesterMK2IdProperty = configuration.getItem("logisticsPipeRequesterMK2.id", LOGISTICSPIPE_REQUEST_MK2_ID);
		logisticPipeRequesterMK2IdProperty.comment = "The item id for the requesting logistics pipe MK2";

		Property logisticPipeProviderMK2IdProperty = configuration.getItem("logisticsPipeProviderMK2.id", LOGISTICSPIPE_PROVIDER_MK2_ID);
		logisticPipeProviderMK2IdProperty.comment = "The item id for the provider logistics pipe MK2";

		Property logisticPipeApiaristAnalyserIdProperty = configuration.getItem("logisticsPipeApiaristAnalyser.id", LOGISTICSPIPE_APIARIST_ANALYSER_ID);
		logisticPipeApiaristAnalyserIdProperty.comment = "The item id for the apiarist logistics analyser pipe";

		Property logisticPipeRemoteOrdererIdProperty = configuration.getItem("logisticsPipeRemoteOrderer.id", LOGISTICSPIPE_REMOTE_ORDERER_ID);
		logisticPipeRemoteOrdererIdProperty.comment = "The item id for the remote orderer logistics pipe";
		
		Property logisticPipeApiaristSinkIdProperty = configuration.getItem("logisticsPipeApiaristSink.id", LOGISTICSPIPE_APIARIST_SINK_ID);
		logisticPipeApiaristSinkIdProperty.comment = "The item id for the apiarist logistics sink pipe";

		Property logisticModuleIdProperty = configuration.getItem("logisticsModules.id", ItemModuleId);
		logisticModuleIdProperty.comment = "The item id for the modules";

		Property logisticUpgradeIdProperty = configuration.getItem("logisticsUpgrades.id", ItemUpgradeId);
		logisticUpgradeIdProperty.comment = "The item id for the upgrades";

		Property logisticUpgradeManagerIdProperty = configuration.getItem("logisticsUpgradeManager.id", ItemUpgradeManagerId);
		logisticUpgradeManagerIdProperty.comment = "The item id for the upgrade manager";

		Property logisticItemDiskIdProperty = configuration.getItem("logisticsDisk.id", ItemDiskId);
		logisticItemDiskIdProperty.comment = "The item id for the disk";
		
		Property logisticItemHUDIdProperty = configuration.getItem("logisticsHUD.id", ItemHUDId);
		logisticItemHUDIdProperty.comment = "The item id for the Logistics HUD glasses";

		Property logisticItemPartsIdProperty = configuration.getItem("logisticsHUDParts.id", ItemPartsId);
		logisticItemPartsIdProperty.comment = "The item id for the Logistics item parts";

		Property logisticCraftingSignCreatorIdProperty = configuration.getItem("logisticsCraftingSignCreator.id", LOGISTICSCRAFTINGSIGNCREATOR_ID);
		logisticCraftingSignCreatorIdProperty.comment = "The item id for the crafting sign creator";
		
		Property logisticPipeBuilderSupplierIdProperty = configuration.getItem("logisticsPipeBuilderSupplier.id", LOGISTICSPIPE_BUILDERSUPPLIER_ID);
		logisticPipeBuilderSupplierIdProperty.comment = "The item id for the builder supplier pipe";
		
		Property logisticPipeLiquidSupplierIdProperty = configuration.getItem("logisticsPipeLiquidSupplier.id", LOGISTICSPIPE_LIQUIDSUPPLIER_ID);
		logisticPipeLiquidSupplierIdProperty.comment = "The item id for the liquid supplier pipe";

		Property logisticInvSysConIdProperty = configuration.getItem("logisticInvSysCon.id", LOGISTICSPIPE_INVSYSCON_ID);
		logisticInvSysConIdProperty.comment = "The item id for the inventory system connector pipe";

		Property logisticEntranceIdProperty = configuration.getItem("logisticEntrance.id", LOGISTICSPIPE_ENTRANCE_ID);
		logisticEntranceIdProperty.comment = "The item id for the logistics system entrance pipe";

		Property logisticDestinationIdProperty = configuration.getItem("logisticDestination.id", LOGISTICSPIPE_DESTINATION_ID);
		logisticDestinationIdProperty.comment = "The item id for the logistics system destination pipe";

		Property logisticItemCardIdProperty = configuration.getItem("logisticItemCard.id", ItemCardId);
		logisticItemCardIdProperty.comment = "The item id for the logistics item card";
		
		//DEBUG (TEST) ONLY
		Property logisticsLiquidContainerIdProperty = null;
		if(LogisticsPipes.DEBUG) {
			logisticsLiquidContainerIdProperty = configuration.getItem("LogisticsLiquidContainer.id", ItemLiquidContainerId);
			logisticsLiquidContainerIdProperty.comment = "The item id for the logistics liquid container";
		}
		
		Property detectionLength = configuration.get(Configuration.CATEGORY_GENERAL, "detectionLength", LOGISTICS_DETECTION_LENGTH);
		detectionLength.comment = "The maximum shortest length between logistics pipes. This is an indicator on the maxim depth of the recursion algorithm to discover logistics neighbours. A low value might use less CPU, a high value will allow longer pipe sections";
		
		Property detectionCount = configuration.get(Configuration.CATEGORY_GENERAL, "detectionCount", LOGISTICS_DETECTION_COUNT);
		detectionCount.comment = "The maximum number of buildcraft pipees (including forks) between logistics pipes. This is an indicator of the maximum ammount of nodes the recursion algorithm will visit before giving up. As it is possible to fork a pipe connection using standard BC pipes the algorithm will attempt to discover all available destinations through that pipe. Do note that the logistics system will not interfere with the operation of non-logistics pipes. So a forked pipe will usually be sup-optimal, but it is possible. A low value might reduce CPU usage, a high value will be able to handle more complex pipe setups. If you never fork your connection between the logistics pipes this has the same meaning as detectionLength and the lower of the two will be used";
		
		Property detectionFrequency = configuration.get(Configuration.CATEGORY_GENERAL, "detectionFrequency", LOGISTICS_DETECTION_FREQUENCY);
		detectionFrequency.comment = "The amount of time that passes between checks to see if it is still connected to its neighbours. A low value will mean that it will detect changes faster but use more CPU. A high value means detection takes longer, but CPU consumption is reduced. A value of 20 will check about every second";
		
		Property countInvertWheelProperty = configuration.get(Configuration.CATEGORY_GENERAL, "ordererCountInvertWheel", LOGISTICS_ORDERER_COUNT_INVERTWHEEL);
		countInvertWheelProperty.comment = "Inverts the the mouse wheel scrolling for remote order number of items"; 

		Property pageInvertWheelProperty = configuration.get(Configuration.CATEGORY_GENERAL, "ordererPageInvertWheel", LOGISTICS_ORDERER_PAGE_INVERTWHEEL);
		pageInvertWheelProperty.comment = "Inverts the the mouse wheel scrolling for remote order pages";

		Property pageDisplayPopupProperty = configuration.get(Configuration.CATEGORY_GENERAL, "displayPopup", displayPopup);
		pageDisplayPopupProperty.comment = "Set the default configuration for the popup of the Orderer Gui. Should it be used?";

		Property logisticsSignId = configuration.getBlock("logisticsSignId", LOGISTICS_SIGN_ID);
		logisticsSignId.comment = "The ID of the LogisticsPipes Sign";
		
		Property logisticsSolidBlockId = configuration.getBlock("logisticsSolidBlockId", LOGISTICS_SOLID_BLOCK_ID);
		logisticsSolidBlockId.comment = "The ID of the LogisticsPipes Solid Block";
		
		Property logisticsPowerUsageDisable = configuration.get(Configuration.CATEGORY_GENERAL, "powerUsageDisabled", LOGISTICS_POWER_USAGE_DISABLED);
		logisticsPowerUsageDisable.comment = "Diable the power usage trough LogisticsPipes";
		
		Property logisticsTileGenericReplacementDisable = configuration.get(Configuration.CATEGORY_GENERAL, "TileReplaceDisabled", LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED);
		logisticsTileGenericReplacementDisable.comment = "Diable the Replacement of the TileGenericPipe trough the LogisticsTileGenericPipe";
		
		Property logisticsHUDRenderDistance = configuration.get(Configuration.CATEGORY_GENERAL, "HUDRenderDistance", LOGISTICS_HUD_RENDER_DISTANCE);
		logisticsHUDRenderDistance.comment = "The max. distance between a player and the HUD that get's shown in blocks.";
		
		Property mandatoryCarpenterRecipes = configuration.get(Configuration.CATEGORY_GENERAL, "mandatoryCarpenterRecipes", MANDATORY_CARPENTER_RECIPES);
		mandatoryCarpenterRecipes.comment = "Whether or not the Carpenter is required to craft Forestry related pipes/modules.";
		
		Property enableParticleFX = configuration.get(Configuration.CATEGORY_GENERAL, "enableParticleFX", ENABLE_PARTICLE_FX);
		enableParticleFX.comment = "Whether or not special particles will spawn.";
		
		Property powerUsageMultiplyerPref = configuration.get(Configuration.CATEGORY_GENERAL, "powerUsageMultiplyer", powerUsageMultiplyer);
		powerUsageMultiplyerPref.comment = "A Multiplyer for the power usage.";
		
		Property multiThread = configuration.get(CATEGORY_MULTITHREAD, "enabled", multiThreadEnabled);
		multiThread.comment = "Enabled the Logistics Pipes multiThread function to allow the network.";
		
		Property multiThreadCount = configuration.get(CATEGORY_MULTITHREAD, "count", multiThreadNumber);
		multiThreadCount.comment = "Number of running Threads.";
		
		Property multiThreadPrio = configuration.get(CATEGORY_MULTITHREAD, "priority", multiThreadPriority);
		multiThreadPrio.comment = "Priority of the multiThread Threads. 10 is highest, 5 normal, 1 lowest";
		
		LOGISTICSNETWORKMONITOR_ID			= Integer.parseInt(logisticNetworkMonitorIdProperty.value);
		LOGISTICSREMOTEORDERER_ID			= Integer.parseInt(logisticRemoteOrdererIdProperty.value);
		ItemModuleId						= Integer.parseInt(logisticModuleIdProperty.value);
		ItemUpgradeId						= Integer.parseInt(logisticUpgradeIdProperty.value);
		ItemUpgradeManagerId				= Integer.parseInt(logisticUpgradeManagerIdProperty.value);
		ItemDiskId							= Integer.parseInt(logisticItemDiskIdProperty.value);
		ItemCardId							= Integer.parseInt(logisticItemCardIdProperty.value);
		ItemHUDId							= Integer.parseInt(logisticItemHUDIdProperty.value);
		ItemPartsId							= Integer.parseInt(logisticItemPartsIdProperty.value);

		//DEBUG (TEST) ONLY
		if(LogisticsPipes.DEBUG) {
			ItemLiquidContainerId				= Integer.parseInt(logisticsLiquidContainerIdProperty.value);
		}
		 
		LOGISTICSPIPE_BASIC_ID 				= Integer.parseInt(logisticPipeIdProperty.value);
		LOGISTICSPIPE_REQUEST_ID			= Integer.parseInt(logisticPipeRequesterIdProperty.value);
		LOGISTICSPIPE_PROVIDER_ID			= Integer.parseInt(logisticPipeProviderIdProperty.value);
		LOGISTICSPIPE_CRAFTING_ID			= Integer.parseInt(logisticPipeCraftingIdProperty.value);
		LOGISTICSPIPE_SATELLITE_ID			= Integer.parseInt(logisticPipeSatelliteIdProperty.value);
		LOGISTICSPIPE_SUPPLIER_ID			= Integer.parseInt(logisticPipeSupplierIdProperty.value);
		LOGISTICSPIPE_CHASSI1_ID			= Integer.parseInt(logisticPipeChassi1IdProperty.value);
		LOGISTICSPIPE_CHASSI2_ID			= Integer.parseInt(logisticPipeChassi2IdProperty.value);
		LOGISTICSPIPE_CHASSI3_ID			= Integer.parseInt(logisticPipeChassi3IdProperty.value);
		LOGISTICSPIPE_CHASSI4_ID			= Integer.parseInt(logisticPipeChassi4IdProperty.value);
		LOGISTICSPIPE_CHASSI5_ID			= Integer.parseInt(logisticPipeChassi5IdProperty.value);
		LOGISTICSPIPE_CRAFTING_MK2_ID		= Integer.parseInt(logisticPipeCraftingMK2IdProperty.value);
		LOGISTICSPIPE_CRAFTING_MK3_ID       = Integer.parseInt(logisticPipeCraftingMK3IdProperty.value);
		LOGISTICSPIPE_REQUEST_MK2_ID		= Integer.parseInt(logisticPipeRequesterMK2IdProperty.value);
		LOGISTICSPIPE_PROVIDER_MK2_ID		= Integer.parseInt(logisticPipeProviderMK2IdProperty.value);
		LOGISTICSPIPE_REMOTE_ORDERER_ID		= Integer.parseInt(logisticPipeRemoteOrdererIdProperty.value);
		LOGISTICSPIPE_APIARIST_ANALYSER_ID	= Integer.parseInt(logisticPipeApiaristAnalyserIdProperty.value);
		LOGISTICSPIPE_APIARIST_SINK_ID		= Integer.parseInt(logisticPipeApiaristSinkIdProperty.value);
		LOGISTICSPIPE_ENTRANCE_ID			= Integer.parseInt(logisticEntranceIdProperty.value);
		LOGISTICSPIPE_DESTINATION_ID		= Integer.parseInt(logisticDestinationIdProperty.value);
		LOGISTICSPIPE_INVSYSCON_ID			= Integer.parseInt(logisticInvSysConIdProperty.value);
		LOGISTICS_SIGN_ID 					= Integer.parseInt(logisticsSignId.value);
		LOGISTICS_SOLID_BLOCK_ID 			= Integer.parseInt(logisticsSolidBlockId.value);
		LOGISTICSPIPE_FIREWALL_ID			= logisticPipeFirewallIdProperty.getInt();

		//DEBUG (TEST) ONLY (LIQUID)
		if(LogisticsPipes.DEBUG) {
			LOGISTICSPIPE_LIQUID_CONNECTOR	= logisticPipeLiquidConnectorIdProperty.getInt();
			LOGISTICSPIPE_LIQUID_BASIC		= logisticPipeLiquidBasicIdProperty.getInt();
			LOGISTICSPIPE_LIQUID_INSERTION	= logisticPipeLiquidInsertionIdProperty.getInt();
			LOGISTICSPIPE_LIQUID_PROVIDER	= logisticPipeLiquidProviderIdProperty.getInt();
			LOGISTICSPIPE_LIQUID_REQUEST	= logisticPipeLiquidRequestIdProperty.getInt();
		}
		
		LOGISTICS_DETECTION_LENGTH			= Integer.parseInt(detectionLength.value);
		LOGISTICS_DETECTION_COUNT			= Integer.parseInt(detectionCount.value);
		LOGISTICS_DETECTION_FREQUENCY 		= Math.max(Integer.parseInt(detectionFrequency.value), 1);
		LOGISTICS_ORDERER_COUNT_INVERTWHEEL = Boolean.parseBoolean(countInvertWheelProperty.value);
		LOGISTICS_ORDERER_PAGE_INVERTWHEEL 	= Boolean.parseBoolean(pageInvertWheelProperty.value);
		
		LOGISTICS_POWER_USAGE_DISABLED	 	= Boolean.parseBoolean(logisticsPowerUsageDisable.value);
		LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED = Boolean.parseBoolean(logisticsTileGenericReplacementDisable.value);
		
		LOGISTICSCRAFTINGSIGNCREATOR_ID		= Integer.parseInt(logisticCraftingSignCreatorIdProperty.value);

		LOGISTICS_HUD_RENDER_DISTANCE 		= Integer.parseInt(logisticsHUDRenderDistance.value);
		
		displayPopup 						= Boolean.parseBoolean(pageDisplayPopupProperty.value);

		LOGISTICSPIPE_BUILDERSUPPLIER_ID	= Integer.parseInt(logisticPipeBuilderSupplierIdProperty.value);
		LOGISTICSPIPE_LIQUIDSUPPLIER_ID		= Integer.parseInt(logisticPipeLiquidSupplierIdProperty.value);
		MANDATORY_CARPENTER_RECIPES			= Boolean.parseBoolean(mandatoryCarpenterRecipes.value);
		ENABLE_PARTICLE_FX					= Boolean.parseBoolean(enableParticleFX.value);
	
		multiThreadEnabled					= multiThread.getBoolean(multiThreadEnabled);
		multiThreadNumber					= multiThreadCount.getInt();
		if(multiThreadNumber < 1) {
			multiThreadNumber = 1;
			multiThreadCount.value = Integer.toString(multiThreadNumber);
		}
		multiThreadPriority					= multiThreadPrio.getInt();
		if(multiThreadPriority < 1 || multiThreadPriority > 10) {
			multiThreadPriority = Thread.NORM_PRIORITY;
			multiThreadPrio.value = Integer.toString(Thread.NORM_PRIORITY);
		}
		
		powerUsageMultiplyer = powerUsageMultiplyerPref.getInt();
		
		if(powerUsageMultiplyer < 1) {
			powerUsageMultiplyer = 1;
			powerUsageMultiplyerPref.value = "1";
		}
		
		configuration.save();
	}

	public static void savePopupState() {
		Property pageDisplayPopupProperty = configuration.get(Configuration.CATEGORY_GENERAL, "displayPopup", displayPopup);
		pageDisplayPopupProperty.comment = "Set the default configuration for the popup of the Orderer Gui. Should it be used?";
		pageDisplayPopupProperty.value = Boolean.toString(displayPopup);
		configuration.save();
	}
}
