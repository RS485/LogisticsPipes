package logisticspipes.network.abstractpackets;

import java.io.IOException;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class DirectionModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private ForgeDirection direction;

	public DirectionModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeForgeDirection(direction);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		direction = input.readForgeDirection();
	}
}
