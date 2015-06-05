package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class DirectionModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private ForgeDirection direction;

	public DirectionModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeForgeDirection(direction);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		direction = data.readForgeDirection();
	}
}
