package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketRenderFX extends PacketCoordinates {

	public int particle;
	public int amount = 0;
	
	public PacketRenderFX() {
		super();
	}
	
	public PacketRenderFX(int id, int x, int y, int z, int particle, int amount) {
		super(id, x, y, z);
		this.particle = particle;
		this.amount = amount;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(particle);
		data.writeInt(amount);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		particle = data.readInt();
		amount = data.readInt();
	}

	
}
