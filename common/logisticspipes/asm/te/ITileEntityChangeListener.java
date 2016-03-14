package logisticspipes.asm.te;

import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.util.EnumFacing;

public interface ITileEntityChangeListener {

	public void pipeRemoved(DoubleCoordinates pos);

	public void pipeAdded(DoubleCoordinates pos, EnumFacing side);

	public void pipeModified(DoubleCoordinates pos);
}
