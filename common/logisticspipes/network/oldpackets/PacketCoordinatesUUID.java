package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PacketCoordinatesUUID extends PacketCoordinates {
	
	public UUID uuid;
	
	public PacketCoordinatesUUID() {
		super();
	}
	
	public PacketCoordinatesUUID(int id, int x, int y, int z, UUID uuid) {
		super(id, x, y, z);
		this.uuid = uuid;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeLong(uuid.getMostSignificantBits());
		data.writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		uuid = new UUID(data.readLong(), data.readLong());
	}
}
