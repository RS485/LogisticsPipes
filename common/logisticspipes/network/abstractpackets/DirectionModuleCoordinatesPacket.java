package logisticspipes.network.abstractpackets;

import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class DirectionModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private EnumFacing direction;

	public DirectionModuleCoordinatesPacket(int id) {
		super(id);
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
