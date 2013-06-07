package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPipeUpdate extends PacketCoordinates {
	
	private PacketPayload payload;
	
	public PacketPipeUpdate() {
		super();
	}

	public PacketPipeUpdate(int id, int x, int y, int z, PacketPayload payload) {
		super(id, x, y, z);
		this.payload = payload;
	}

	public PacketPayload getPayload() {
		return payload;
	}
	
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		// No payload means no data
		if (payload == null) {
			data.writeInt(0);
			data.writeInt(0);
			data.writeInt(0);
			return;
		}

		data.writeInt(payload.intPayload.length);
		data.writeInt(payload.floatPayload.length);
		data.writeInt(payload.stringPayload.length);

		for (int intData : payload.intPayload)
			data.writeInt(intData);
		for (float floatData : payload.floatPayload)
			data.writeFloat(floatData);
		for (String stringData : payload.stringPayload)
			data.writeUTF(stringData);

	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		
		payload = new PacketPayload();

		payload.intPayload = new int[data.readInt()];
		payload.floatPayload = new float[data.readInt()];
		payload.stringPayload = new String[data.readInt()];

		for (int i = 0; i < payload.intPayload.length; i++)
			payload.intPayload[i] = data.readInt();
		for (int i = 0; i < payload.floatPayload.length; i++)
			payload.floatPayload[i] = data.readFloat();
		for (int i = 0; i < payload.stringPayload.length; i++)
			payload.stringPayload[i] = data.readUTF();

	}

}
