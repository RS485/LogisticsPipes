package logisticspipes.asm;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.relauncher.FMLInjectionData;

public class LogisticsAccessTransformer extends AccessTransformer {
	
	public LogisticsAccessTransformer() throws IOException {
		super("lp_at.cfg");
	}
}
