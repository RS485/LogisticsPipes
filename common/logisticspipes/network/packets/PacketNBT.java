package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.SendNBTTagCompound;
import net.minecraft.nbt.NBTTagCompound;

public class PacketNBT extends LogisticsPipesPacket {

	private final int id;
	public NBTTagCompound tag;
	
	@Override
	public int getID() {
		return id;
	}

	public PacketNBT() {
		super();
		id = 0;
	}

	public PacketNBT(int id, NBTTagCompound tag) {
		super();
		this.id = id;
		this.tag = tag;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		tag = SendNBTTagCompound.readNBTTagCompound(data);
	}
}
