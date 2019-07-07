package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ItemPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummySlot;
import logisticspipes.utils.gui.FluidSlot;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetGhostItemPacket extends ItemPacket {

	@Getter
	@Setter
	public int integer;

	public SetGhostItemPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		Container container = player.openContainer;

		if (container != null) {
			if (getInteger() >= 0 && getInteger() < container.inventorySlots.size()) {
				Slot slot = container.getSlot(getInteger());

				if (slot instanceof DummySlot || slot instanceof FluidSlot) {
					slot.putStack(getStack());
				}
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(integer);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		integer = input.readInt();
	}

	@Override
	public ModernPacket template() {
		return new SetGhostItemPacket(getId());
	}
}
