package logisticspipes.config;

import java.io.File;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Configs {

	// Ids
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

	//GuiOrderer Popup setting
	public static boolean displayPopup = true;
	
	//BlockID
	public static int LOGISTICS_SIGN_ID = 1100;
	public static int LOGISTICS_SOLID_BLOCK_ID = 1101;
	
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
		
		Property logisticRemoteOrdererIdProperty = configuration.get("logisticsRemoteOrderer.id", Configuration.CATEGORY_ITEM, LOGISTICSREMOTEORDERER_ID);
		logisticRemoteOrdererIdProperty.comment = "The item id for the remote orderer";
		
		Property logisticNetworkMonitorIdProperty = configuration.get("logisticsNetworkMonitor.id", Configuration.CATEGORY_ITEM, LOGISTICSNETWORKMONITOR_ID);
		logisticNetworkMonitorIdProperty.comment = "The item id for the network monitor";
		
		Property logisticPipeIdProperty = configuration.get("logisticsPipe.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_BASIC_ID);
		logisticPipeIdProperty.comment = "The item id for the basic logistics pipe";
		
		Property logisticPipeRequesterIdProperty = configuration.get("logisticsPipeRequester.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REQUEST_ID);
		logisticPipeRequesterIdProperty.comment = "The item id for the requesting logistics pipe";
		
		Property logisticPipeProviderIdProperty = configuration.get("logisticsPipeProvider.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_PROVIDER_ID);
		logisticPipeProviderIdProperty.comment = "The item id for the providing logistics pipe";
		
		Property logisticPipeCraftingIdProperty = configuration.get("logisticsPipeCrafting.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_ID);
		logisticPipeCraftingIdProperty.comment = "The item id for the crafting logistics pipe";

		Property logisticPipeSatelliteIdProperty = configuration.get("logisticsPipeSatellite.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_SATELLITE_ID);
		logisticPipeSatelliteIdProperty.comment = "The item id for the crafting satellite pipe";
		
		Property logisticPipeSupplierIdProperty = configuration.get("logisticsPipeSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_SUPPLIER_ID);
		logisticPipeSupplierIdProperty.comment = "The item id for the supplier pipe";
		
		Property logisticPipeChassi1IdProperty = configuration.get("logisticsPipeChassi1.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI1_ID);
		logisticPipeChassi1IdProperty.comment = "The item id for the chassi1";
		
		Property logisticPipeChassi2IdProperty = configuration.get("logisticsPipeChassi2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI2_ID);
		logisticPipeChassi2IdProperty.comment = "The item id for the chassi2";
		
		Property logisticPipeChassi3IdProperty = configuration.get("logisticsPipeChassi3.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI3_ID);
		logisticPipeChassi3IdProperty.comment = "The item id for the chassi3";
		
		Property logisticPipeChassi4IdProperty = configuration.get("logisticsPipeChassi4.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI4_ID);
		logisticPipeChassi4IdProperty.comment = "The item id for the chassi4";
		
		Property logisticPipeChassi5IdProperty = configuration.get("logisticsPipeChassi5.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI5_ID);
		logisticPipeChassi5IdProperty.comment = "The item id for the chassi5";

		Property logisticPipeCraftingMK2IdProperty = configuration.get("logisticsPipeCraftingMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_MK2_ID);
		logisticPipeCraftingMK2IdProperty.comment = "The item id for the crafting logistics pipe MK2";
		
		Property logisticPipeCraftingMK3IdProperty = configuration.get("logisticsPipeCraftingMK3.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_MK3_ID);
		logisticPipeCraftingMK3IdProperty.comment = "The item id for the crafting logistics pipe MK3";
		
		Property logisticPipeRequesterMK2IdProperty = configuration.get("logisticsPipeRequesterMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REQUEST_MK2_ID);
		logisticPipeRequesterMK2IdProperty.comment = "The item id for the requesting logistics pipe MK2";

		Property logisticPipeProviderMK2IdProperty = configuration.get("logisticsPipeProviderMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_PROVIDER_MK2_ID);
		logisticPipeProviderMK2IdProperty.comment = "The item id for the provider logistics pipe MK2";

		Property logisticPipeApiaristAnalyserIdProperty = configuration.get("logisticsPipeApiaristAnalyser.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_APIARIST_ANALYSER_ID);
		logisticPipeApiaristAnalyserIdProperty.comment = "The item id for the apiarist logistics analyser pipe";

		Property logisticPipeRemoteOrdererIdProperty = configuration.get("logisticsPipeRemoteOrderer.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REMOTE_ORDERER_ID);
		logisticPipeRemoteOrdererIdProperty.comment = "The item id for the remote orderer logistics pipe";
		
		Property logisticPipeApiaristSinkIdProperty = configuration.get("logisticsPipeApiaristSink.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_APIARIST_SINK_ID);
		logisticPipeApiaristSinkIdProperty.comment = "The item id for the apiarist logistics sink pipe";

		Property logisticModuleIdProperty = configuration.get("logisticsModules.id", Configuration.CATEGORY_ITEM, ItemModuleId);
		logisticModuleIdProperty.comment = "The item id for the modules";

		Property logisticItemDiskIdProperty = configuration.get("logisticsDisk.id", Configuration.CATEGORY_ITEM, ItemDiskId);
		logisticItemDiskIdProperty.comment = "The item id for the disk";
		
		Property logisticItemHUDIdProperty = configuration.get("logisticsHUD.id", Configuration.CATEGORY_ITEM, ItemHUDId);
		logisticItemHUDIdProperty.comment = "The item id for the Logistics HUD glasses";

		Property logisticItemPartsIdProperty = configuration.get("logisticsHUDParts.id", Configuration.CATEGORY_ITEM, ItemPartsId);
		logisticItemPartsIdProperty.comment = "The item id for the Logistics item parts";

		Property logisticCraftingSignCreatorIdProperty = configuration.get("logisticsCraftingSignCreator.id", Configuration.CATEGORY_ITEM, LOGISTICSCRAFTINGSIGNCREATOR_ID);
		logisticCraftingSignCreatorIdProperty.comment = "The item id for the crafting sign creator";
		
		Property logisticPipeBuilderSupplierIdProperty = configuration.get("logisticsPipeBuilderSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_BUILDERSUPPLIER_ID);
		logisticPipeBuilderSupplierIdProperty.comment = "The item id for the builder supplier pipe";
		
		Property logisticPipeLiquidSupplierIdProperty = configuration.get("logisticsPipeLiquidSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_LIQUIDSUPPLIER_ID);
		logisticPipeLiquidSupplierIdProperty.comment = "The item id for the liquid supplier pipe";

		Property logisticInvSysConIdProperty = configuration.get("logisticInvSysCon.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_INVSYSCON_ID);
		logisticInvSysConIdProperty.comment = "The item id for the inventory system connector pipe";

		Property logisticEntranceIdProperty = configuration.get("logisticEntrance.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_ENTRANCE_ID);
		logisticEntranceIdProperty.comment = "The item id for the logistics system entrance pipe";

		Property logisticDestinationIdProperty = configuration.get("logisticDestination.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_DESTINATION_ID);
		logisticDestinationIdProperty.comment = "The item id for the logistics system destination pipe";

		Property logisticItemCardIdProperty = configuration.get("logisticItemCard.id", Configuration.CATEGORY_ITEM, ItemCardId);
		logisticItemCardIdProperty.comment = "The item id for the logistics item card";

		
		Property detectionLength = configuration.get("detectionLength", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_LENGTH);
		detectionLength.comment = "The maximum shortest length between logistics pipes. This is an indicator on the maxim depth of the recursion algorithm to discover logistics neighbours. A low value might use less CPU, a high value will allow longer pipe sections";
		
		Property detectionCount = configuration.get("detectionCount", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_COUNT);
		detectionCount.comment = "The maximum number of buildcraft pipees (including forks) between logistics pipes. This is an indicator of the maximum ammount of nodes the recursion algorithm will visit before giving up. As it is possible to fork a pipe connection using standard BC pipes the algorithm will attempt to discover all available destinations through that pipe. Do note that the logistics system will not interfere with the operation of non-logistics pipes. So a forked pipe will usually be sup-optimal, but it is possible. A low value might reduce CPU usage, a high value will be able to handle more complex pipe setups. If you never fork your connection between the logistics pipes this has the same meaning as detectionLength and the lower of the two will be used";
		
		Property detectionFrequency = configuration.get("detectionFrequency", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_FREQUENCY);
		detectionFrequency.comment = "The amount of time that passes between checks to see if it is still connected to its neighbours. A low value will mean that it will detect changes faster but use more CPU. A high value means detection takes longer, but CPU consumption is reduced. A value of 20 will check about every second";
		
		Property countInvertWheelProperty = configuration.get("ordererCountInvertWheel", Configuration.CATEGORY_GENERAL, LOGISTICS_ORDERER_COUNT_INVERTWHEEL);
		countInvertWheelProperty.comment = "Inverts the the mouse wheel scrolling for remote order number of items"; 

		Property pageInvertWheelProperty = configuration.get("ordererPageInvertWheel", Configuration.CATEGORY_GENERAL, LOGISTICS_ORDERER_PAGE_INVERTWHEEL);
		pageInvertWheelProperty.comment = "Inverts the the mouse wheel scrolling for remote order pages";

		Property pageDisplayPopupProperty = configuration.get("displayPopup", Configuration.CATEGORY_GENERAL, displayPopup);
		pageDisplayPopupProperty.comment = "Set the default configuration for the popup of the Orderer Gui. Should it be used?";

		if(configuration.categories.containsKey(Configuration.CATEGORY_BLOCK) && configuration.categories.get(Configuration.CATEGORY_BLOCK).containsKey("logisticsBlockId")) {
			Property logisticsBlockId = configuration.get("logisticsBlockId", Configuration.CATEGORY_BLOCK, LOGISTICS_SIGN_ID);
			LOGISTICS_SIGN_ID = Integer.parseInt(logisticsBlockId.value);
			configuration.categories.get(Configuration.CATEGORY_BLOCK).remove("logisticsBlockId");
		}
		Property logisticsSignId = configuration.get("logisticsSignId", Configuration.CATEGORY_BLOCK, LOGISTICS_SIGN_ID);
		logisticsSignId.comment = "The ID of the LogisticsPipes Sign";
		
		Property logisticsSolidBlockId = configuration.get("logisticsSolidBlockId", Configuration.CATEGORY_BLOCK, LOGISTICS_SOLID_BLOCK_ID);
		logisticsSolidBlockId.comment = "The ID of the LogisticsPipes Solid Block";
		
		Property logsiticsPowerUsageDisable = configuration.get("powerUsageDisabled", Configuration.CATEGORY_GENERAL, LOGISTICS_POWER_USAGE_DISABLED);
		logsiticsPowerUsageDisable.comment = "Diable the power usage trough LogisticsPipes";
		
		Property logsiticsTileGenericReplacementDisable = configuration.get("TileReplaceDisabled", Configuration.CATEGORY_GENERAL, LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED);
		logsiticsTileGenericReplacementDisable.comment = "Diable the Replacement of the TileGenericPipe trough the LogisticsTileGenericPipe";
		
		Property logsiticsHUDRenderDistance = configuration.get("HUDRenderDistance", Configuration.CATEGORY_GENERAL, LOGISTICS_HUD_RENDER_DISTANCE);
		logsiticsHUDRenderDistance.comment = "The max. distance between a player and the HUD that get's shown in blocks.";
		
		configuration.save();
		
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
		
		LOGISTICS_POWER_USAGE_DISABLED	 	= Boolean.parseBoolean(logsiticsPowerUsageDisable.value);
		LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED = Boolean.parseBoolean(logsiticsTileGenericReplacementDisable.value);
		
		LOGISTICSCRAFTINGSIGNCREATOR_ID		= Integer.parseInt(logisticCraftingSignCreatorIdProperty.value);

		LOGISTICS_HUD_RENDER_DISTANCE 		= Integer.parseInt(logsiticsHUDRenderDistance.value);
		
		displayPopup 						= Boolean.parseBoolean(pageDisplayPopupProperty.value);

		LOGISTICSPIPE_BUILDERSUPPLIER_ID	= Integer.parseInt(logisticPipeBuilderSupplierIdProperty.value);
		LOGISTICSPIPE_LIQUIDSUPPLIER_ID		= Integer.parseInt(logisticPipeLiquidSupplierIdProperty.value);
	}

	public static void savePopupState() {
		Property pageDisplayPopupProperty = configuration.get("displayPopup", Configuration.CATEGORY_GENERAL, displayPopup);
		pageDisplayPopupProperty.comment = "Set the default configuration for the popup of the Orderer Gui. Should it be used?";
		pageDisplayPopupProperty.value = Boolean.toString(displayPopup);
		configuration.save();
	}
}
