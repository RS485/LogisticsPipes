package logisticspipes.config;

import java.io.File;
import java.util.Arrays;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import logisticspipes.LogisticsPipes;

//@formatter:off
//CHECKSTYLE:OFF

public class Configs {

	public static final String CATEGORY_MULTITHREAD = "multithread";
	public static final String CATEGORY_ASYNC = "async";

	private static Configuration CONFIGURATION;

	// Configrables
	public static int LOGISTICS_DETECTION_LENGTH = 50;
	public static int LOGISTICS_DETECTION_COUNT = 100;
	public static int LOGISTICS_DETECTION_FREQUENCY = 20 * 30;
	public static boolean LOGISTICS_ORDERER_COUNT_INVERTWHEEL = false;
	public static boolean LOGISTICS_ORDERER_PAGE_INVERTWHEEL = false;
	public static final float LOGISTICS_ROUTED_SPEED_MULTIPLIER = 20F;
	public static final float LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER = 10F;
	public static int MAX_UNROUTED_CONNECTIONS = 32;

	public static int LOGISTICS_HUD_RENDER_DISTANCE = 15;

	public static float	pipeDurability = 0.25F; //TODO

	public static boolean LOGISTICS_POWER_USAGE_DISABLED = false;
	public static double POWER_USAGE_MULTIPLIER = 1;
	public static double COMPILER_SPEED = 1.0;
	public static boolean ENABLE_RESEARCH_SYSTEM = false;

	public static int LOGISTICS_CRAFTING_TABLE_POWER_USAGE = 250;

	public static boolean TOOLTIP_INFO = LogisticsPipes.isDEBUG();
	public static boolean ENABLE_PARTICLE_FX = true;

	public static int[] CHASSIS_SLOTS_ARRAY = {1,2,3,4,8};

	// GuiOrderer Popup setting
	public static boolean DISPLAY_POPUP = true;

	// MultiThread
	public static int MULTI_THREAD_NUMBER = 4;
	public static int MULTI_THREAD_PRIORITY = Thread.NORM_PRIORITY;
	public static int ASYNC_THRESHOLD = 100;

	public static boolean CHECK_FOR_UPDATES = true;

	public static boolean EASTER_EGGS = true;

	public static boolean OPAQUE = false;

	public static int MAX_ROBOT_DISTANCE = 64;

	private static boolean loaded = false;
	public static void load() {
		if(Configs.loaded) {
			return;
		}
		if(Loader.instance().getConfigDir() == null) {
			return;
		}
		Configs.CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "LogisticsPipes.cfg"));
		Configs.CONFIGURATION.load();
		Configs.loaded = true;

		if (Configs.CONFIGURATION.hasCategory("logisticspipe.id")
				|| Configs.CONFIGURATION.hasCategory("logisticsPipe.id")) {
			throw new RuntimeException(
					"Old config, please remove it and manually reconfigure LogisticPipes");
		}

		Configs.LOGISTICS_DETECTION_LENGTH = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"detectionLength",
						Configs.LOGISTICS_DETECTION_LENGTH,
						"The maximum shortest length between logistics pipes. This is an indicator on the maxim depth of the recursion algorithm to discover logistics neighbours. A low value might use less CPU, a high value will allow longer pipe sections")
						.getInt();
		Configs.LOGISTICS_DETECTION_COUNT = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"detectionCount",
						Configs.LOGISTICS_DETECTION_COUNT,
						"The maximum number of buildcraft pipes (including forks) between logistics pipes. This is an indicator of the maximum amount of nodes the recursion algorithm will visit before giving up. As it is possible to fork a pipe connection using standard BC pipes the algorithm will attempt to discover all available destinations through that pipe. Do note that the logistics system will not interfere with the operation of non-logistics pipes. So a forked pipe will usually be sup-optimal, but it is possible. A low value might reduce CPU usage, a high value will be able to handle more complex pipe setups. If you never fork your connection between the logistics pipes this has the same meaning as detectionLength and the lower of the two will be used")
						.getInt();
		Configs.LOGISTICS_DETECTION_FREQUENCY = Math
				.max(Configs.CONFIGURATION
						.get(Configuration.CATEGORY_GENERAL,
								"reDetectionFrequency",
								Configs.LOGISTICS_DETECTION_FREQUENCY,
								"The amount of time that passes between checks to see if it is still connected to its neighbours (Independently from block place detection). A low value will mean that it will correct wrong values faster but use more CPU. A high value means error correction takes longer, but CPU consumption is reduced. A value of 20 will check about every second (default 600 [30 seconds])")
								.getInt(), 1);

		Configs.MAX_ROBOT_DISTANCE = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL, "maxRobotDistance",
						Configs.MAX_ROBOT_DISTANCE,
						"The max. distance between two robots when there is no zone defined.")
						.getInt();
		Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL, "ordererCountInvertWheel",
						Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL,
						"Inverts the the mouse wheel scrolling for remote order number of items")
						.getBoolean(false);
		Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL = Configs.CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "ordererPageInvertWheel",
				Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL,
				"Inverts the the mouse wheel scrolling for remote order pages")
				.getBoolean(false);

		Configs.LOGISTICS_POWER_USAGE_DISABLED = Configs.CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "powerUsageDisabled",
				Configs.LOGISTICS_POWER_USAGE_DISABLED,
				"Disable the power usage trough LogisticsPipes").getBoolean(
						false);

		Configs.COMPILER_SPEED = Configs.CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "compilerSpeed",
				Configs.COMPILER_SPEED,
				"Multiplier for the work speed of the compiler").getDouble(
						1.0);

		Configs.LOGISTICS_HUD_RENDER_DISTANCE = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL, "HUDRenderDistance",
						Configs.LOGISTICS_HUD_RENDER_DISTANCE,
						"The max. distance between a player and the HUD that get's shown in blocks.")
						.getInt();

		Configs.DISPLAY_POPUP = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"displayPopup",
						Configs.DISPLAY_POPUP,
						"Set the default configuration for the popup of the Orderer Gui. Should it be used?")
						.getBoolean(false);

		Configs.ENABLE_PARTICLE_FX = Configs.CONFIGURATION.get(Configuration.CATEGORY_GENERAL,
				"enableParticleFX", Configs.ENABLE_PARTICLE_FX,
				"Whether or not special particles will spawn.")
				.getBoolean(false);

		if(Configs.CONFIGURATION.hasKey(Configs.CATEGORY_MULTITHREAD, "enabled")) {
			//ConfigCategory.remove is deprecated, but there's no other way to remove a key-value pair without completely recreating the config...
			Configs.CONFIGURATION.getCategory(Configs.CATEGORY_MULTITHREAD).remove("enabled");
		}
		Configs.MULTI_THREAD_NUMBER = Configs.CONFIGURATION.get(Configs.CATEGORY_MULTITHREAD, "count",
				Configs.MULTI_THREAD_NUMBER, "Number of routing table update Threads, 0 to disable.").getInt();
		if (Configs.MULTI_THREAD_NUMBER < 0) {
			Configs.MULTI_THREAD_NUMBER = 0;
			Configs.CONFIGURATION.get(Configs.CATEGORY_MULTITHREAD, "count",
					Configs.MULTI_THREAD_NUMBER, "Number of routing table update Threads, 0 to disable.").set(Integer
							.toString(Configs.MULTI_THREAD_NUMBER));
		}
		Configs.MULTI_THREAD_PRIORITY = Configs.CONFIGURATION
				.get(Configs.CATEGORY_MULTITHREAD, "priority", Configs.MULTI_THREAD_PRIORITY,
						"Priority of the multiThread Threads. 10 is highest, 5 normal, 1 lowest")
						.getInt();
		if (Configs.MULTI_THREAD_PRIORITY < 1 || Configs.MULTI_THREAD_PRIORITY > 10) {
			Configs.MULTI_THREAD_PRIORITY = Thread.NORM_PRIORITY;
			Configs.CONFIGURATION
			.get(Configs.CATEGORY_MULTITHREAD, "priority",
					Configs.MULTI_THREAD_PRIORITY,
					"Priority of the multiThread Threads. 10 is highest, 5 normal, 1 lowest").set(Integer
							.toString(Thread.NORM_PRIORITY));
		}
		Configs.ASYNC_THRESHOLD = Configs.CONFIGURATION.get(Configs.CATEGORY_ASYNC, "threshold", Configs.ASYNC_THRESHOLD,
				"Threshold for running asynchronous code. A lower value will make async calls with small networks where the impact is low. Low values might hurt performance").getInt();


		Configs.POWER_USAGE_MULTIPLIER = Configs.CONFIGURATION.get(
				Configuration.CATEGORY_GENERAL, "powerUsageMultiplyer",
				Configs.POWER_USAGE_MULTIPLIER, "A Multiplyer for the power usage.")
				.getDouble(Configs.POWER_USAGE_MULTIPLIER);

		if (Configs.POWER_USAGE_MULTIPLIER <= 0) {
			Configs.POWER_USAGE_MULTIPLIER = 1;
			Configs.CONFIGURATION.get(Configuration.CATEGORY_GENERAL,
					"powerUsageMultiplyer", Configs.POWER_USAGE_MULTIPLIER,
					"A Multiplyer for the power usage.").set(1);
		}

		Configs.LOGISTICS_CRAFTING_TABLE_POWER_USAGE = Math
				.max(Configs.CONFIGURATION
						.get(Configuration.CATEGORY_GENERAL,
								"logisticsCraftingTablePowerUsage",
								Configs.LOGISTICS_CRAFTING_TABLE_POWER_USAGE,
								"Number of LPower units the Logistics Crafting Table uses per craft.")
								.getInt(), 0);

		Configs.CHECK_FOR_UPDATES = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"checkForUpdates",
						Configs.CHECK_FOR_UPDATES,
						"Should LogisticsPipes check for updates?")
						.getBoolean(false);

		Configs.OPAQUE = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"OpaquePipes",
						Configs.OPAQUE,
						"Render every LP pipe opaque.")
						.getBoolean(false);

		Configs.EASTER_EGGS = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"easterEggs",
						Configs.EASTER_EGGS,
						"Do you fancy easter eggs?")
						.getBoolean(false);

		Configs.CHASSIS_SLOTS_ARRAY = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL, "chassisSlots",
						Configs.CHASSIS_SLOTS_ARRAY,
						"The number of slots in a chassis pipe starting from MK1 to MK5. Because there are 5 tiers, there need to be 5 values (positive integers, zero is allowed).")
						.getIntList();

		if (Configs.CHASSIS_SLOTS_ARRAY.length != 5) {
			throw new RuntimeException(
					"The config file of Logistics Pipes needs to have 5 values (positive integers, zero is allowed) in ascending order in chassisSlots. \nThe configuration contains "
							+ Configs.CHASSIS_SLOTS_ARRAY.length + " values.");
		}

		for (int i = 0; i < Configs.CHASSIS_SLOTS_ARRAY.length; i++) {
			if (Configs.CHASSIS_SLOTS_ARRAY[i] < 0)
				throw new RuntimeException(
						"The config file of Logistics Pipes needs to have 5 values (positive integers, zero is allowed) in ascending order in chassisSlots. \nThe configuration contains "
								+ Configs.CHASSIS_SLOTS_ARRAY[i] + " as one of the values.");
		}
		Arrays.sort(Configs.CHASSIS_SLOTS_ARRAY);

		Configs.CONFIGURATION.save();
	}

	public static void savePopupState() {
		Property pageDisplayPopupProperty = Configs.CONFIGURATION
				.get(Configuration.CATEGORY_GENERAL,
						"displayPopup",
						Configs.DISPLAY_POPUP,
						"Set the default configuration for the popup of the Orderer Gui. Should it be used?");
		pageDisplayPopupProperty.set(Boolean.toString(Configs.DISPLAY_POPUP));
		Configs.CONFIGURATION.save();
	}
}
