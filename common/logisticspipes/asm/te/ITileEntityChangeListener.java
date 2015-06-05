package logisticspipes.asm.te;

import logisticspipes.utils.tuples.LPPosition;

import net.minecraftforge.common.util.ForgeDirection;

public interface ITileEntityChangeListener {

	public void pipeRemoved(LPPosition pos);

	public void pipeAdded(LPPosition pos, ForgeDirection side);

	public void pipeModified(LPPosition pos);
}
