package logisticspipes.network.abstractpackets;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.UpgradeSlot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import java.io.IOException;

public abstract class SlotPacket extends ModernPacket {

	@Setter(AccessLevel.PRIVATE)
	@Getter(AccessLevel.PROTECTED)
	private int integer;

	public SlotPacket(int id) {
		super(id);
	}

	public <T extends Slot> T getSlot(EntityPlayer player, Class<T> clazz) {
		if(player.openContainer instanceof DummyContainer) {
			if (getInteger() >= player.openContainer.inventorySlots.size()) {
				targetNotFound("The requested Slot was out of range");
			} else {
				Slot slot = player.openContainer.getSlot(getInteger());
				if (slot == null) {
					targetNotFound("The requested Slot was null");
				} else if(!clazz.isAssignableFrom(slot.getClass())) {
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
	public void readData(LPDataInputStream data) throws IOException {
		setInteger(data.readInt());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(getInteger());
	}

	protected void targetNotFound(String message) {
		throw new TargetNotFoundException(message, this);
	}
}
