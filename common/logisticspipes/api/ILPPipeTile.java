package logisticspipes.api;

import net.minecraft.util.math.BlockPos;

/**
 * Public interface implemented by LP's Pipe tile
 */
public interface ILPPipeTile {

	/**
	 * Used to access the internal pipe logic This also can return the
	 * IRequestAPI or IRoutedPowerProvider
	 *
	 * @return the pipe
	 */
	ILPPipe getLPPipe();

	BlockPos getBlockPos();
}
