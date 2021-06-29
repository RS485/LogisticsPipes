package logisticspipes;

import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import logisticspipes.blocks.BlockDummy;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericSubMultiBlock;

public class LPBlocks {

	// Logistics Blocks
	@ObjectHolder("logisticspipes:solid_block")
	public static BlockDummy dummy;

	@ObjectHolder("logisticspipes:frame")
	public static LogisticsSolidBlock frame;

	@ObjectHolder("logisticspipes:power_junction")
	public static LogisticsSolidBlock powerJunction;

	@ObjectHolder("logisticspipes:security_station")
	public static LogisticsSolidBlock securityStation;

	@ObjectHolder("logisticspipes:crafting_table")
	public static LogisticsSolidBlock crafter;

	@ObjectHolder("logisticspipes:crafting_table_fuzzy")
	public static LogisticsSolidBlock crafterFuzzy;

	@ObjectHolder("logisticspipes:statistics_table")
	public static LogisticsSolidBlock statisticsTable;

	@ObjectHolder("logisticspipes:power_provider_rf")
	public static LogisticsSolidBlock powerProviderRF;

	@ObjectHolder("logisticspipes:power_provider_eu")
	public static LogisticsSolidBlock powerProviderEU;

	@ObjectHolder("logisticspipes:power_provider_mj")
	public static LogisticsSolidBlock powerProviderMJ;

	@ObjectHolder("logisticspipes:program_compiler")
	public static LogisticsSolidBlock programCompiler;

	@ObjectHolder("logisticspipes:pipe")
	public static LogisticsBlockGenericPipe pipe;

	@ObjectHolder("logisticspipes:sub_multiblock")
	public static LogisticsBlockGenericSubMultiBlock subMultiblock;

}
