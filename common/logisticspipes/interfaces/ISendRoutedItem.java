package logisticspipes.interfaces;

import java.util.UUID;

import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import net.minecraft.item.ItemStack;

public interface ISendRoutedItem {
	public UUID getSourceUUID();
	public void sendStack(ItemStack stack);
	public void sendStack(ItemStack stack, UUID destination);
	public void sendStack(ItemStack stack, UUID destination, ItemSendMode mode);
}
