package logisticspipes.asm.te;

import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.util.EnumFacing;

public interface ITileEntityChangeListener {

	void pipeRemoved(DoubleCoordinates pos);

	void pipeAdded(DoubleCoordinates pos, EnumFacing side);

	void pipeModified(DoubleCoordinates pos);
}
