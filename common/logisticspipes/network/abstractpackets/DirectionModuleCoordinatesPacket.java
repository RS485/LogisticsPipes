package logisticspipes.network.abstractpackets;

import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class DirectionModuleCoordinatesPacket extends ModuleCoordinatesPacket {
	private EnumFacing direction;

	public DirectionModuleCoordinatesPacket(int id) {
		super(id);
	}

	public DirectionModuleCoordinatesPacket setDirection(EnumFacing newDirection) {
		direction = newDirection;
		return this;
	}

	public EnumFacing getDirection() {
		return direction;
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeFacing(direction);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		direction = input.readFacing();
	}
}
