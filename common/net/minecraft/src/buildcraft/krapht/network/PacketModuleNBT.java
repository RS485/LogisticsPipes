package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.NBTTagCompound;

public class PacketModuleNBT extends PacketCoordinates {
	public NBTTagCompound tag;
	public int slot;

	public PacketModuleNBT() {
		super();
	}

	public PacketModuleNBT(int id, int x, int y, int z, int slot, NBTTagCompound tag) {
		super(id, x, y, z);
		this.tag = tag;
		this.slot = slot;
	}

	public PacketModuleNBT(int id, int x, int y, int z, int slot,INBTPacketProvider provider) {
		super(id, x, y, z);
		
		this.tag = new NBTTagCompound();
		this.slot = slot;
		provider.writeToPacketNBT(this.tag);
	}
	
	public void handle(INBTPacketProvider provider) {
		if(tag != null) {
			provider.readFromPacketNBT(this.tag);
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
		tag = SendNBTTagCompound.readNBTTagCompound(data);
	}
}
