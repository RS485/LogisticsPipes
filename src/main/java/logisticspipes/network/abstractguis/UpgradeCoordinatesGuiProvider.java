package logisticspipes.network.abstractguis;

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

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PRIVATE)
	private int positionInt;

	public UpgradeCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(positionInt);
	}

	@Override
	public void readData(LPDataInput input) {
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
				throw new TargetNotFoundException("The requested Slot was out of range", this);
			} else {
				Slot slot = player.openContainer.getSlot(positionInt);
				if (slot == null) {
					throw new TargetNotFoundException("The requested Slot was null", this);
				} else if (!clazz.isAssignableFrom(slot.getClass())) {
					throw new TargetNotFoundException("Couldn't find " + clazz.getName() + ", found slot with " + slot.getClass(), this);
				} else {
					return (T) slot;
				}
			}
		}
		return null;
	}

}
