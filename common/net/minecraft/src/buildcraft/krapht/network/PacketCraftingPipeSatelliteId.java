package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketCraftingPipeSatelliteId extends PacketCoordinates {
	private int packetId = NetworkConstants.CRAFTING_PIPE_SATELLITE_ID;
	public int satelliteId;

	public PacketCraftingPipeSatelliteId(int satelliteId) {
		super();
		this.satelliteId = satelliteId;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		
		if(satelliteId < 0)
			satelliteId = 0;
		
		data.write(this.satelliteId);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		// We do not accept a satellite id from the client
	}

	@Override
	public int getID() {
		return packetId;
	}
}
