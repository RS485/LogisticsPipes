package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class InventoryModuleCoordinatesPacket extends ModuleCoordinatesPacket {

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

	public InventoryModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		if (inventory != null) {
			output.writeByte(0);
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				output.writeByte(i);
				sendItemStack(inventory.getStackInSlot(i), output);
			}
			output.writeByte(-1); // mark packet end
		} else if (stackList != null) {
			output.writeByte(0);
			for (int i = 0; i < stackList.size(); i++) {
				output.writeByte(i);
				sendItemStack(stackList.get(i), output);
			}
			output.writeByte(-1); // mark packet end
		} else if (identList != null) {
			output.writeByte(1);
			for (ItemIdentifierStack stack : identList) {
				if (stack == null) {
					output.writeByte(0);
					continue;
				}
				output.writeByte(1);
				output.writeItemIdentifierStack(stack);
			}
			output.writeByte(-1);
		} else if (identSet != null) {
			output.writeByte(1);
			for (ItemIdentifierStack stack : identSet) {
				if (stack == null) {
					output.writeByte(0);
					continue;
				}
				output.writeByte(1);
				output.writeItemIdentifierStack(stack);
			}
			output.writeByte(-1);
		} else {
			throw new UnsupportedOperationException("Can't send this Packet without content");
		}
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		byte mode = input.readByte();
		if (mode == 0) {
			stackList = new LinkedList<>();
			byte index = input.readByte();
			while (index != -1) { // read until the end
				((LinkedList<ItemStack>) stackList).addLast(readItemStack(input));
				index = input.readByte(); // read the next slot
			}
		} else if (mode == 1) {
			identList = new LinkedList<>();
			byte index = input.readByte();
			while (index != -1) { // read until the end
				if (index == 0) {
					((LinkedList<ItemIdentifierStack>) identList).addLast(null);
				} else {
					((LinkedList<ItemIdentifierStack>) identList).addLast(input.readItemIdentifierStack());
				}
				index = input.readByte(); // read the next slot
			}
		} else {
			throw new UnsupportedOperationException("Unknown receive mode: " + mode);
		}
	}

	private void sendItemStack(ItemStack itemstack, LPDataOutput output) throws IOException {
		if (itemstack != null) {
			output.writeInt(Item.getIdFromItem(itemstack.getItem()));
			output.writeInt(itemstack.stackSize);
			output.writeInt(itemstack.getItemDamage());
			output.writeNBTTagCompound(itemstack.getTagCompound());
		} else {
			output.writeInt(0);
		}
	}

	private ItemStack readItemStack(LPDataInput input) throws IOException {
		final int itemID = input.readInt();
		if (itemID == 0) {
			return null;
		} else {
			int stackSize = input.readInt();
			int damage = input.readInt();
			ItemStack stack = new ItemStack(Item.getItemById(itemID), stackSize, damage);
			stack.setTagCompound(input.readNBTTagCompound());
			return stack;
		}
	}
}
