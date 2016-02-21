package logisticspipes.asm.te;

import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.util.EnumFacing;


public interface ITileEntityChangeListener {

	public void pipeRemoved(LPPosition pos);

	public void pipeAdded(LPPosition pos, EnumFacing side);

	public void pipeModified(LPPosition pos);
}
