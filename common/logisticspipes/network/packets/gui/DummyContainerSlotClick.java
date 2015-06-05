package logisticspipes.network.packets.gui;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.gui.ColorSlot;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummySlot;
import logisticspipes.utils.gui.FluidSlot;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class DummyContainerSlotClick extends ModernPacket {

	@Getter
	@Setter
	int slotId;

	@Getter
	@Setter
	ItemStack stack;

	@Getter
	@Setter
	int button;

	public DummyContainerSlotClick(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		slotId = data.readInt();
		stack = data.readItemIdentifierStack().makeNormalStack();
		button = data.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (player instanceof EntityPlayerMP && ((EntityPlayerMP) player).openContainer instanceof DummyContainer) {
			DummyContainer container = (DummyContainer) ((EntityPlayerMP) player).openContainer;
			Slot slot = (Slot) container.inventorySlots.get(slotId);
			if (slot instanceof DummySlot || slot instanceof ColorSlot || slot instanceof FluidSlot) {
				container.handleDummyClick(slot, slotId, stack, button, 0, player);
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(slotId);
		data.writeItemIdentifierStack(ItemIdentifierStack.getFromStack(stack));
		data.writeInt(button);
	}

	@Override
	public ModernPacket template() {
		return new DummyContainerSlotClick(getId());
	}
}
