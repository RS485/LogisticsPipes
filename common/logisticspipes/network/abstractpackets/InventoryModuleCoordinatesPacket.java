package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		if (inventory != null) {
			data.writeByte(0);
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				data.writeByte(i);
				sendItemStack(inventory.getStackInSlot(i), data);
			}
			data.writeByte(-1); // mark packet end
		} else if (stackList != null) {
			data.writeByte(0);
			for (int i = 0; i < stackList.size(); i++) {
				data.writeByte(i);
				sendItemStack(stackList.get(i), data);
			}
			data.writeByte(-1); // mark packet end
		} else if (identList != null) {
			data.writeByte(1);
			for (ItemIdentifierStack stack : identList) {
				if (stack == null) {
					data.writeByte(0);
					continue;
				}
				data.writeByte(1);
				data.writeItemIdentifierStack(stack);
			}
			data.writeByte(-1);
		} else if (identSet != null) {
			data.writeByte(1);
			for (ItemIdentifierStack stack : identSet) {
				if (stack == null) {
					data.writeByte(0);
					continue;
				}
				data.writeByte(1);
				data.writeItemIdentifierStack(stack);
			}
			data.writeByte(-1);
		} else {
			throw new UnsupportedOperationException("Can't send this Packet without content");
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		byte mode = data.readByte();
		if (mode == 0) {
			stackList = new LinkedList<ItemStack>();
			byte index = data.readByte();
			while (index != -1) { // read until the end
				((LinkedList<ItemStack>) stackList).addLast(readItemStack(data));
				index = data.readByte(); // read the next slot
			}
		} else if (mode == 1) {
			identList = new LinkedList<ItemIdentifierStack>();
			byte index = data.readByte();
			while (index != -1) { // read until the end
				if (index == 0) {
					((LinkedList<ItemIdentifierStack>) identList).addLast(null);
				} else {
					((LinkedList<ItemIdentifierStack>) identList).addLast(data.readItemIdentifierStack());
				}
				index = data.readByte(); // read the next slot
			}
		} else {
			throw new UnsupportedOperationException("Unknown receive mode: " + mode);
		}
	}

	private void sendItemStack(ItemStack itemstack, LPDataOutputStream data) throws IOException {
		if (itemstack != null) {
			data.writeInt(Item.getIdFromItem(itemstack.getItem()));
			data.writeInt(itemstack.stackSize);
			data.writeInt(itemstack.getItemDamage());
			data.writeNBTTagCompound(itemstack.getTagCompound());
		} else {
			data.writeInt(0);
		}
	}

	private ItemStack readItemStack(LPDataInputStream data) throws IOException {
		final int itemID = data.readInt();
		if (itemID == 0) {
			return null;
		} else {
			int stackSize = data.readInt();
			int damage = data.readInt();
			ItemStack stack = new ItemStack(Item.getItemById(itemID), stackSize, damage);
			stack.setTagCompound(data.readNBTTagCompound());
			return stack;
		}
	}
}
