package logisticspipes;

import net.minecraftforge.fml.common.registry.GameRegistry;

import logisticspipes.blocks.BlockDummy;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericSubMultiBlock;

public class LPBlocks {

	// Logistics Blocks
	@GameRegistry.ObjectHolder("logisticspipes:solid_block")
	public static BlockDummy dummy;

	@GameRegistry.ObjectHolder("logisticspipes:frame")
	public static LogisticsSolidBlock frame;

	@GameRegistry.ObjectHolder("logisticspipes:power_junction")
	public static LogisticsSolidBlock powerJunction;

	@GameRegistry.ObjectHolder("logisticspipes:security_station")
	public static LogisticsSolidBlock securityStation;

	@GameRegistry.ObjectHolder("logisticspipes:crafting_table")
	public static LogisticsSolidBlock crafter;

	@GameRegistry.ObjectHolder("logisticspipes:crafting_table_fuzzy")
	public static LogisticsSolidBlock crafterFuzzy;

	@GameRegistry.ObjectHolder("logisticspipes:statistics_table")
	public static LogisticsSolidBlock statisticsTable;

	@GameRegistry.ObjectHolder("logisticspipes:power_provider_rf")
	public static LogisticsSolidBlock powerProviderRF;

	@GameRegistry.ObjectHolder("logisticspipes:power_provider_eu")
	public static LogisticsSolidBlock powerProviderEU;

	@GameRegistry.ObjectHolder("logisticspipes:power_provider_mj")
	public static LogisticsSolidBlock powerProviderMJ;

	@GameRegistry.ObjectHolder("logisticspipes:program_compiler")
	public static LogisticsSolidBlock programCompiler;

	@GameRegistry.ObjectHolder("logisticspipes:pipe")
	public static LogisticsBlockGenericPipe pipe;

	@GameRegistry.ObjectHolder("logisticspipes:sub_multiblock")
	public static LogisticsBlockGenericSubMultiBlock subMultiblock;

}
