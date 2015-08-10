package logisticspipes.asm.te;

import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraftforge.common.util.ForgeDirection;

public interface ITileEntityChangeListener {

	public void pipeRemoved(DoubleCoordinates pos);

	public void pipeAdded(DoubleCoordinates pos, ForgeDirection side);

	public void pipeModified(DoubleCoordinates pos);
}
