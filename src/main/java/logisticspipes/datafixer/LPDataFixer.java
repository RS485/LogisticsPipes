package logisticspipes.datafixer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

import logisticspipes.LPConstants;

public class LPDataFixer {

	public static final LPDataFixer INSTANCE = new LPDataFixer();

	public static final int VERSION = 1;

	private LPDataFixer() {}

	public void init() {
		ModFixs mf = FMLCommonHandler.instance().getDataFixer().init(LPConstants.LP_MOD_ID, VERSION);
		mf.registerFix(DataFixerTE.TYPE, new DataFixerTE());
		mf.registerFix(DataFixerSolidBlockItems.TYPE, new DataFixerSolidBlockItems());
		MinecraftForge.EVENT_BUS.register(new MissingMappingHandler());
	}

}
