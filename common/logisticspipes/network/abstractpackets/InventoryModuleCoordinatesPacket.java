package logisticspipes.network.abstractpackets;

import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class InventoryModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	private static final byte STACK_MARKER = 0;
	private static final byte IDENT_MARKER = 1;

	private IInventory inventory;

	private NonNullList<ItemStack> stackList;

	private List<ItemIdentifierStack> identList;

	private Set<ItemIdentifierStack> identSet;

	public InventoryModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);

		if (inventory != null) {
			output.writeByte(STACK_MARKER);
			output.writeInt(inventory.getSizeInventory());
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				output.writeItemStack(inventory.getStackInSlot(i));
			}
		} else if (stackList != null) {
			output.writeByte(STACK_MARKER);
			output.writeCollection(stackList, LPDataOutput::writeItemStack);
		} else if (identList != null) {
			output.writeByte(IDENT_MARKER);
			output.writeCollection(identList, LPDataOutput::writeItemIdentifierStack);
		} else if (identSet != null) {
			output.writeByte(IDENT_MARKER);
			output.writeCollection(identSet, LPDataOutput::writeItemIdentifierStack);
		} else {
			throw new IllegalStateException("Wont send packet without content");
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);

		byte marker = input.readByte();
		if (marker == STACK_MARKER) {
			stackList = input.readNonNullList(LPDataInput::readItemStack, ItemStack.EMPTY);
		} else if (marker == IDENT_MARKER) {
			identList = input.readLinkedList(LPDataInput::readItemIdentifierStack);
		} else {
			throw new UnsupportedOperationException("Unknown marker: " + marker);
		}
	}

	public NonNullList<ItemStack> getStackList() {
		return this.stackList;
	}

	public InventoryModuleCoordinatesPacket setStackList(NonNullList<ItemStack> stackList) {
		this.stackList = stackList;
		return this;
	}

	public List<ItemIdentifierStack> getIdentList() {
		return this.identList;
	}

	public InventoryModuleCoordinatesPacket setIdentList(List<ItemIdentifierStack> identList) {
		this.identList = identList;
		return this;
	}

	public InventoryModuleCoordinatesPacket setInventory(IInventory inventory) {
		this.inventory = inventory;
		return this;
	}

	public InventoryModuleCoordinatesPacket setIdentSet(Set<ItemIdentifierStack> identSet) {
		this.identSet = identSet;
		return this;
	}
}
