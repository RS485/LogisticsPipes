package logisticspipes.network.abstractpackets;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ItemPacket extends CoordinatesPacket {

	@Getter
	@Setter
	@Nonnull
	private ItemStack stack = ItemStack.EMPTY;

	public ItemPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		if (getStack().isEmpty()) {
			output.writeInt(0);
		} else {
			output.writeInt(Item.getIdFromItem(getStack().getItem()));
			output.writeInt(getStack().getCount());
			output.writeInt(getStack().getItemDamage());
			output.writeNBTTagCompound(getStack().getTagCompound());
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);

		final int itemID = input.readInt();
		if (itemID == 0) {
			setStack(ItemStack.EMPTY);
		} else {
			int stackSize = input.readInt();
			int damage = input.readInt();
			setStack(new ItemStack(Item.getItemById(itemID), stackSize, damage));
			getStack().setTagCompound(input.readNBTTagCompound());
		}
	}
}
