package logisticspipes.network.abstractpackets;

import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.utils.item.ItemStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class InventoryModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	private static final byte STACK_MARKER = 0;
	private static final byte IDENT_MARKER = 1;

	@Setter
	private IInventory inventory;

	@Getter
	@Setter
	private List<ItemStack> stackList;

	@Getter
	@Setter
	private List<ItemStack> identList;

	@Setter
	private Set<ItemStack> identSet;

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
			output.writeCollection(identList, LPDataOutput::writeItemStack);
		} else if (identSet != null) {
			output.writeByte(IDENT_MARKER);
			output.writeCollection(identSet, LPDataOutput::writeItemStack);
		} else {
			throw new IllegalStateException("Wont send packet without content");
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);

		byte marker = input.readByte();
		if (marker == STACK_MARKER) {
			stackList = input.readLinkedList(LPDataInput::readItemStack);
		} else if (marker == IDENT_MARKER) {
			identList = input.readLinkedList(LPDataInput::readItemStack);
		} else {
			throw new UnsupportedOperationException("Unknown marker: " + marker);
		}
	}
}
