package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.src.ItemStack;

public class PacketItem extends PacketCoordinates {

	public ItemStack itemstack;

	public PacketItem() {
		super();
	}

	public PacketItem(int id, int x, int y, int z, ItemStack stack) {
		super(id,x,y,z);
		this.itemstack = stack;
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		if(itemstack != null) {
			data.writeInt(itemstack.itemID);
			data.writeInt(itemstack.stackSize);
			data.writeInt(itemstack.getItemDamage());
			SendNBTTagCompound.writeNBTTagCompound(itemstack.getTagCompound(), data);
		} else {
			data.writeInt(0);
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		
		final int itemID = data.readInt();
		if(itemID != 0) {
			itemstack = new ItemStack(itemID, data.readInt(), data.readInt());
			itemstack.setTagCompound(SendNBTTagCompound.readNBTTagCompound(data));
		} else {
			itemstack = null;
		}
	}
}
