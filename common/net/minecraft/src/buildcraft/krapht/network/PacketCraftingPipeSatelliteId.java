package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketCraftingPipeSatelliteId extends PacketCoordinates {
	public int satelliteId;
	
	public PacketCraftingPipeSatelliteId() {
		super();
	}

	public PacketCraftingPipeSatelliteId(int x, int y, int z, int satelliteId) { // TODO Coordinates
		super(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID, x, y, z);
		this.satelliteId = satelliteId;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		
		if(satelliteId < 0) {
			satelliteId = 0;
		}
		
		data.write(this.satelliteId);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		
		this.satelliteId = data.readInt();
	}
}
