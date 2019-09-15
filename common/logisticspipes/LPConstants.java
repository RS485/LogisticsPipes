package logisticspipes;

import network.rs485.logisticspipes.LogisticsPipesKt;

public class LPConstants {

	private LPConstants() {}

	@Deprecated
	public static final String LP_MOD_ID = LogisticsPipesKt.ModID;

	public static final float FACADE_THICKNESS = 2F / 16F;
	public static final float PIPE_NORMAL_SPEED = 0.01F;
	public static final float PIPE_MIN_POS = 0.1875F;
	public static final float PIPE_MAX_POS = 0.8125F;
	public static final float BC_PIPE_MIN_POS = 0.25F;
	public static final float BC_PIPE_MAX_POS = 0.75F;

	public static final boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");
	public static final String MCVersion = "%MCVERSION%";
	public static final String VERSION = "%VERSION%:%DEBUG%";
	public static final boolean DEV_BUILD = LPConstants.VERSION.contains(".dev.") || LPConstants.DEBUG;

	public static final String computerCraftModID = "computercraft";
	public static final String openComputersModID = "opencomputers";
	public static final String ic2ModID = "ic2";
	public static final String bcSiliconModID = "buildcraftsilicon";
	public static final String bcTransportModID = "buildcrafttransport";
	public static final String railcraftModID = "railcraft";
	public static final String tubestuffModID = "tubestuff";
	public static final String thermalExpansionModID = "thermalexpansion";
	public static final String enderCoreModID = "endercore";
	public static final String betterStorageModID = "betterstorage";
	public static final String neiModID = "notenoughitems";
	public static final String factorizationModID = "factorization";
	public static final String enderioModID = "enderio";
	public static final String thermalDynamicsModID = "thermaldynamics";
	public static final String cclrenderModID = "cclrender";
	public static final String ironChestModID = "ironchest";
	public static final String cofhCoreModID = "cofhcore";
	public static final String mcmpModID = "mcmultipart";

}
