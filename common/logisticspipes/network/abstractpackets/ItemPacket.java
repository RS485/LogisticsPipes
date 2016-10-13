package logisticspipes.network.abstractpackets;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ItemPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemStack stack;

	public ItemPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		if (getStack() != null) {
			output.writeInt(Item.getIdFromItem(getStack().getItem()));
			output.writeInt(getStack().stackSize);
			output.writeInt(getStack().getItemDamage());
			output.writeNBTTagCompound(getStack().getTagCompound());
		} else {
			output.writeInt(0);
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);

		final int itemID = input.readInt();
		if (itemID != 0) {
			int stackSize = input.readInt();
			int damage = input.readInt();
			setStack(new ItemStack(Item.getItemById(itemID), stackSize, damage));
			getStack().setTagCompound(input.readNBTTagCompound());
		} else {
			setStack(null);
		}
	}
}
