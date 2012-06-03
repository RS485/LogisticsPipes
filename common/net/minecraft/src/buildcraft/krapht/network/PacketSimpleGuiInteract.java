package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketSimpleGuiInteract extends PacketCoordinates {
	private int packetId = 0;

	public PacketSimpleGuiInteract(int packetId) { // TODO Coordinates
		super();
		this.packetId = packetId;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
	}

	@Override
	public int getID() {
		return packetId;
	}
}
