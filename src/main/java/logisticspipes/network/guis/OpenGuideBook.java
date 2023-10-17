package logisticspipes.network.guis;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.guidebook.ItemGuideBook;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class OpenGuideBook extends ModernPacket {

	private EnumHand hand;
	private ItemStack stack;

	public OpenGuideBook(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		hand = input.readEnum(EnumHand.class);
		stack = input.readItemStack();
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeEnum(hand);
		output.writeItemStack(stack);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ItemGuideBook.openGuideBook(hand, stack);
	}

	@Override
	public ModernPacket template() {
		return new OpenGuideBook(getId());
	}

	@Nonnull
	public OpenGuideBook setHand(@Nonnull EnumHand hand) {
		this.hand = hand;
		return this;
	}

	@Nonnull
	public OpenGuideBook setStack(@Nonnull ItemStack stack) {
		this.stack = stack;
		return this;
	}
}
