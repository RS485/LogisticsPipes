package logisticspipes.network.packets;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packetcontent.IntegerContent;
import logisticspipes.network.packetcontent.ItemStackContent;
import logisticspipes.network.packetcontent.PacketContentBuilder;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummySlot;
import logisticspipes.utils.gui.FluidSlot;

@StaticResolve
public class SetGhostItemPacket extends ModernPacket {

	private final IntegerContent integer;
	private final ItemStackContent stack;

	public SetGhostItemPacket(int id) {
		super(id);
		PacketContentBuilder builder = new PacketContentBuilder();
		integer = builder.addInteger();
		stack = builder.addItemStack();
		builder.build(this);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		Container container = player.openContainer;

		if (container != null) {
			if (integer.getValue() >= 0 && integer.getValue() < container.inventorySlots.size()) {
				Slot slot = container.getSlot(integer.getValue());

				if (slot instanceof DummySlot || slot instanceof FluidSlot) {
					slot.putStack(stack.getValue());
				}
			}
		}
	}

	@Override
	public ModernPacket template() {
		return new SetGhostItemPacket(getId());
	}

	public SetGhostItemPacket setInteger(int value) {
		integer.setValue(value);
		return this;
	}

	public SetGhostItemPacket setStack(@Nonnull ItemStack value) {
		stack.setValue(value);
		return this;
	}
}
