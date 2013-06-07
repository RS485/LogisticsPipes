package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.SendNBTTagCompound;
import net.minecraft.nbt.NBTTagCompound;

public class PacketNBT extends PacketCoordinates {

	public NBTTagCompound tag;
	
	public PacketNBT() {
		super();
	}

	public PacketNBT(int id, NBTTagCompound tag) {
		super(id, 0, 0, 0);
		this.tag = tag;
	}
	
	public PacketNBT(int id, int x, int y, int z, NBTTagCompound tag) {
		super(id, x, y, z);
		this.tag = tag;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		tag = SendNBTTagCompound.readNBTTagCompound(data);
	}
}
