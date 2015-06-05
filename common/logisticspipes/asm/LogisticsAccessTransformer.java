package logisticspipes.asm;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class LogisticsAccessTransformer extends AccessTransformer {

	public LogisticsAccessTransformer() throws IOException {
		super("logisticspipes_at.cfg");
	}
}
