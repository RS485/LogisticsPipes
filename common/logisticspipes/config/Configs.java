package logisticspipes.config;

import java.io.File;

import logisticspipes.LPConstants;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.Loader;

public class Configs {

	public static final String CATEGORY_MULTITHREAD = "multithread";
	public static final String CATEGORY_DEBUG 		= "debug";

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
	
	public static float	pipeDurability = 0.25F; //TODO

	public static boolean LOGISTICS_POWER_USAGE_DISABLED = false;
	public static boolean ENABLE_RESEARCH_SYSTEM = false;

	public static boolean TOOLTIP_INFO = LPConstants.DEBUG;
	public static boolean MANDATORY_CARPENTER_RECIPES = true;
	public static boolean ENABLE_PARTICLE_FX = true;

	// GuiOrderer Popup setting
	public static boolean DISPLAY_POPUP = true;

	// MultiThread
	public static int MULTI_THREAD_NUMBER = 4;
	public static int MULTI_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	public static double POWER_USAGE_MULTIPLIER = 1;
	public static int LOGISTICS_CRAFTING_TABLE_POWER_USAGE = 250;

	public static boolean CHECK_FOR_UPDATES = true;
	
	public static boolean EASTER_EGGS = true;
	
	public static boolean OPAQUE = false;
	
	private static boolean loaded = false;
	public static void load() {
		if(loaded) return;
		if(Loader.instance().getConfigDir() == null) return;
		CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "LogisticsPipes.cfg"));
		CONFIGURATION.load();
		loaded = true;

		if (CONFIGURATION.hasCategory("logisticspipe.id")
				|| CONFIGURATION.hasCategory("logisticsPipe.id")) {
			throw new RuntimeException(
					"Old config, please remove it and manually reconfigure LogisticPipes");
		}
		
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

		
		POWER_USAGE_MULTIPLIER = CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "powerUsageMultiplyer",
				POWER_USAGE_MULTIPLIER, "A Multiplyer for the power usage.")
				.getDouble(POWER_USAGE_MULTIPLIER);

		if (POWER_USAGE_MULTIPLIER <= 0) {
			POWER_USAGE_MULTIPLIER = 1;
			CONFIGURATION.get(Configuration.CATEGORY_GENERAL,
					"powerUsageMultiplyer", POWER_USAGE_MULTIPLIER,
					"A Multiplyer for the power usage.").set(1);
		}

		LOGISTICS_CRAFTING_TABLE_POWER_USAGE = Math
				.max(CONFIGURATION
						.get(Configuration.CATEGORY_GENERAL,
								"logisticsCraftingTablePowerUsage",
								LOGISTICS_CRAFTING_TABLE_POWER_USAGE,
								"Number of LPower units the Logistics Crafting Table uses per craft.")
						.getInt(), 0);

		CHECK_FOR_UPDATES = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"checkForUpdates",
						CHECK_FOR_UPDATES,
						"Should LogisticsPipes check for updates?")
				.getBoolean(false);
		
		OPAQUE = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"OpaquePipes",
						OPAQUE,
						"Render every LP pipe opaque.")
				.getBoolean(false);
		
		EASTER_EGGS = CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"easterEggs",
						EASTER_EGGS,
						"Do you fancy easter eggs?")
				.getBoolean(false);
		
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
