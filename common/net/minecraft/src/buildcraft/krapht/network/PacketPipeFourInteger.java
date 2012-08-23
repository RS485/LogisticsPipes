package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPipeFourInteger extends PacketCoordinates {
	public int integer1;
	public int integer2;
	public int integer3;
	public int integer4;

	public PacketPipeFourInteger() {
		super();
	}

	public PacketPipeFourInteger(int id, int x, int y, int z, int integer1, int integer2, int integer3, int integer4) {
		super(id, x, y, z);

		this.integer1 = integer1;
		this.integer2 = integer2;
		this.integer3 = integer3;
		this.integer4 = integer4;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		data.writeInt(integer1);
		data.writeInt(integer2);
		data.writeInt(integer3);
		data.writeInt(integer4);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		integer1 = data.readInt();
		integer2 = data.readInt();
		integer3 = data.readInt();
		integer4 = data.readInt();
	}
}
