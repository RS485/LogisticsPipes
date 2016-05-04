package logisticspipes.network.abstractguis;

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

public abstract class UpgradeCoordinatesGuiProvider extends CoordinatesPopupGuiProvider {

	public UpgradeCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PRIVATE)
	private int positionInt;

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(positionInt);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		positionInt = input.readInt();
	}

	public UpgradeCoordinatesGuiProvider setSlot(Slot slot) {
		this.setPositionInt(slot.slotNumber);
		return this;
	}

	public <T extends Slot> T getSlot(EntityPlayer player, Class<T> clazz) {
		if (player.openContainer instanceof DummyContainer) {
			if (positionInt >= player.openContainer.inventorySlots.size()) {
				targetNotFound("The requested Slot was out of range");
			} else {
				Slot slot = player.openContainer.getSlot(positionInt);
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

	protected void targetNotFound(String message) {
		throw new TargetNotFoundException(message, this);
	}
}
