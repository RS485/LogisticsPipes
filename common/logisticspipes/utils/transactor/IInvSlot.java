package logisticspipes.utils.transactor;

import net.minecraft.item.ItemStack;

public interface IInvSlot {

	boolean canPutStackInSlot(ItemStack stack);

	ItemStack getStackInSlot();

	void setStackInSlot(ItemStack stackInSlot);

}
