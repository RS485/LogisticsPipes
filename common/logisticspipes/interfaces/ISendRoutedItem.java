package logisticspipes.interfaces;

import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import net.minecraft.item.ItemStack;

public interface ISendRoutedItem {
	public int getSourceint();
	public void sendStack(ItemStack stack);
	public void sendStack(ItemStack stack, int destination);
	public void sendStack(ItemStack stack, int destination, ItemSendMode mode);
}
