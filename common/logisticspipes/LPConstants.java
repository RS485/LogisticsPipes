package logisticspipes;

public class LPConstants {
	private LPConstants(){}
	
	public static final float FACADE_THICKNESS = 2F / 16F;
	public static final float PIPE_NORMAL_SPEED = 0.01F;
	public static final float PIPE_MIN_POS = 0.2F;
	public static final float PIPE_MAX_POS = 0.8F;
	public static final float BC_PIPE_MIN_POS = 0.25F;
	public static final float BC_PIPE_MAX_POS = 0.75F;

	public static final boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");
	public static final String MCVersion = "%MCVERSION%";
	public static final String VERSION = "%VERSION%:%DEBUG%";
	public static final boolean DEV_BUILD = VERSION.contains(".dev.") || DEBUG;

	public static int	pipeModel = -1;
	public static int	solidBlockModel = -1;
}
