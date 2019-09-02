package logisticspipes.asm.te;

import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface ITileEntityChangeListener {

	void pipeRemoved(DoubleCoordinates pos);

	void pipeAdded(DoubleCoordinates pos, EnumFacing side);

	void pipeModified(DoubleCoordinates pos);
}
