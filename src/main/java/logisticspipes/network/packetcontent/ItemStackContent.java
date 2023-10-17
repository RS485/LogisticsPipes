package logisticspipes.network.packetcontent;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class ItemStackContent implements IPacketContent<ItemStack> {

	@Nonnull
	private ItemStack stack = ItemStack.EMPTY;

	@Override
	@Nonnull
	public ItemStack getValue() {
		return stack;
	}

	@Override
	public void setValue(@Nonnull ItemStack value) {
		stack = value;
	}

	@Override
	public void readData(LPDataInput input) {
		stack = input.readItemStack();
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeItemStack(stack);
	}
}
