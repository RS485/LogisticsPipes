package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

public class PacketInventoryChange extends PacketCoordinates {

	public IInventory inventory;
	public ArrayList<ItemStack> itemStacks;

	public PacketInventoryChange() {
		super();
	}

	public PacketInventoryChange(int id, int x, int y, int z, IInventory inventory) { // TODO Add constructor with Map<slotid, ItemStack>
		super(id, x, y, z);
		this.inventory = inventory;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {

		super.writeData(data);

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			data.writeByte(i);

			final ItemStack itemstack = inventory.getStackInSlot(i);

			if (itemstack != null) {
				data.writeInt(itemstack.itemID);
				data.writeInt(itemstack.stackSize);
				data.writeInt(itemstack.getItemDamage());
				SendNBTTagCompound.writeNBTTagCompound(itemstack.getTagCompound(), data);
			} else {
				data.writeInt(0);
			}
		}
		data.writeByte(-1); // mark packet end
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		super.readData(data);

		itemStacks = new ArrayList<ItemStack>(); // TODO ... => Map<slotid, ItemStack>
		
		byte index = data.readByte();

		while (index != -1) { // read until the end
			final int itemID = data.readInt();
			if (itemID == 0) {
				itemStacks.add(null);
			} else {
				ItemStack stack = new ItemStack(itemID, data.readInt(), data.readInt());
				stack.setTagCompound(SendNBTTagCompound.readNBTTagCompound(data));
				itemStacks.add(stack);
			}
			
			index = data.readByte(); // read the next slot
		}
	}

}
