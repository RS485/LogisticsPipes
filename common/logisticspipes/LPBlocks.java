package logisticspipes;

import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericSubMultiBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class LPBlocks {

	// Logistics Blocks
	@GameRegistry.ObjectHolder("logisticspipes:solid_block")
	public static LogisticsSolidBlock solidBlock;

	@GameRegistry.ObjectHolder("logisticspipes:pipe")
	public static LogisticsBlockGenericPipe pipe;

	@GameRegistry.ObjectHolder("logisticspipes:sub_multiblock")
	public static LogisticsBlockGenericSubMultiBlock subMultiblock;

}
