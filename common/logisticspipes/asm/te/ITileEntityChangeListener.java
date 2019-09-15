package logisticspipes.asm.te;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface ITileEntityChangeListener {

	void pipeRemoved(BlockPos pos);

	void pipeAdded(BlockPos pos, Direction side);

	void pipeModified(BlockPos pos);

}
