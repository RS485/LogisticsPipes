package logisticspipes.network.abstractpackets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class SlotPacket extends ModernPacket {

	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PROTECTED)
	private int integer;

	public SlotPacket(int id) {
		super(id);
	}

	public <T extends Slot> T getSlot(EntityPlayer player, Class<T> clazz) {
		if (player.openContainer instanceof DummyContainer) {
			if (getInteger() >= player.openContainer.inventorySlots.size()) {
				targetNotFound("The requested Slot was out of range");
			} else {
				Slot slot = player.openContainer.getSlot(getInteger());
				if (slot == null) {
					targetNotFound("The requested Slot was null");
				} else if (!clazz.isAssignableFrom(slot.getClass())) {
					targetNotFound("Couldn't find " + clazz.getName() + ", found slot with " + slot.getClass());
				} else {
					return (T) slot;
				}
			}
		}
		return null;
	}

	public SlotPacket setSlot(Slot slot) {
		setInteger(slot.slotNumber);
		return this;
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		setInteger(input.readInt());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(getInteger());
	}

	protected void targetNotFound(String message) {
		throw new TargetNotFoundException(message, this);
	}
}
