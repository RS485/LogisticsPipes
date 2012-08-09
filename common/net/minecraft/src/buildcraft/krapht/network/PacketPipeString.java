package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPipeString extends PacketCoordinates {
	public String string;

	public PacketPipeString() {
		super();
	}

	public PacketPipeString(int id, int x, int y, int z, String string) {
		super(id, x, y, z);

		this.string = string;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		data.writeUTF(string != null ? string : "");
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		string = data.readUTF();
	}
}
