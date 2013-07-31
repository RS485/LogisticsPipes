package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Accessors(chain=true)
public abstract class InventoryCoordinatesPacket extends CoordinatesPacket {
	

	@Setter
	private IInventory inventory;

	@Getter
	@Setter
	private List<ItemStack> stackList;
	
	@Getter
	@Setter
	private List<ItemIdentifierStack> identList;

	@Setter
	private Set<ItemIdentifierStack> identSet;
	
	public InventoryCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		if(inventory != null) {
			data.writeByte(0);
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				data.writeByte(i);
				sendItemStack(inventory.getStackInSlot(i), data);
			}
			data.writeByte(-1); // mark packet end
		} else if(stackList != null) {
			data.writeByte(0);
			for (int i = 0; i < stackList.size(); i++) {
				data.writeByte(i);
				sendItemStack(stackList.get(i), data);
			}
			data.writeByte(-1); // mark packet end
		} else if(identList != null) {
			data.writeByte(1);
			for(ItemIdentifierStack stack:identList) {
				data.writeByte(1);
				sendItemIdentifierStack(stack, data);
			}
			data.writeByte(-1);
		} else if(identSet != null) {
			data.writeByte(1);
			for(ItemIdentifierStack stack:identSet) {
				data.writeByte(1);
				sendItemIdentifierStack(stack, data);
			}
			data.writeByte(-1);
		} else {
			throw new UnsupportedOperationException("Can't send this Packet without content");
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		byte mode = data.readByte();
		if(mode == 0) {
			stackList = new LinkedList<ItemStack>();
			byte index = data.readByte();
			while (index != -1) { // read until the end
				((LinkedList<ItemStack>)stackList).addLast(readItemStack(data));
				index = data.readByte(); // read the next slot
			}
		} else if(mode == 1) {
			identList = new LinkedList<ItemIdentifierStack>();
			byte index = data.readByte();
			while (index != -1) { // read until the end
				((LinkedList<ItemIdentifierStack>)identList).addLast(readItemIdentifierStack(data));
				index = data.readByte(); // read the next slot
			}
		} else {
			throw new UnsupportedOperationException("Unknown receive mode: " + mode);
		}
	}
	
	private void sendItemStack(ItemStack itemstack, DataOutputStream data) throws IOException {
		if (itemstack != null) {
			data.writeInt(itemstack.itemID);
			data.writeInt(itemstack.stackSize);
			data.writeInt(itemstack.getItemDamage());
			SendNBTTagCompound.writeNBTTagCompound(itemstack.getTagCompound(), data);
		} else {
			data.writeInt(0);
		}
	}
	
	private void sendItemIdentifierStack(ItemIdentifierStack item, DataOutputStream data) throws IOException {
		if (item != null) {
			item.write(data);
		} else {
			data.writeInt(0);
		}
	}
	
	private ItemStack readItemStack(DataInputStream data) throws IOException {
		final int itemID = data.readInt();
		if (itemID == 0) {
			return null;
		} else {
			int stackSize = data.readInt();
			int damage = data.readInt();
			ItemStack stack = new ItemStack(itemID, stackSize, damage);
			stack.setTagCompound(SendNBTTagCompound.readNBTTagCompound(data));
			return stack;
		}
	}
	
	private ItemIdentifierStack readItemIdentifierStack(DataInputStream data) throws IOException {
		final int itemID = data.readInt();
		if (itemID == 0) {
			return null;
		} else {
			int stackSize = data.readInt();
			int damage = data.readInt();
			NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
			return new ItemIdentifierStack(ItemIdentifier.get(itemID, damage, tag), stackSize);
		}
	}
}
