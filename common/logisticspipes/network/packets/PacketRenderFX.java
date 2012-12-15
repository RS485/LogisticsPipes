package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketRenderFX extends PacketCoordinates {

	public String particle;
	public int amount = 0;
	
	public PacketRenderFX() {
		super();
	}
	
	public PacketRenderFX(int id, int x, int y, int z, String particle, int amount) {
		super(id, x, y, z);
		this.particle = particle;
		this.amount = amount;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeUTF(particle);
		data.writeInt(amount);
	}
	
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		particle = data.readUTF();
		amount = data.readInt();
	}

	
}
