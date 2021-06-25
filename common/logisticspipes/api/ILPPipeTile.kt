package logisticspipes.api

import net.minecraft.util.math.BlockPos

/**
 * Base interface for all pipe tiles in the LP mod
 */
interface ILPPipeTile {

	/**
	 * Used to access the internal pipe logic.
	 */
	fun getLPPipe(): ILPPipe

	/**
	 * Used to obtain the tile position
	 */
	fun getBlockPos(): BlockPos
}
