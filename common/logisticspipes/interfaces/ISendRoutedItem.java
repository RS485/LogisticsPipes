package logisticspipes.interfaces;

import java.util.UUID;

import logisticspipes.main.CoreRoutedPipe.ItemSendMode;
import net.minecraft.src.ItemStack;

public interface ISendRoutedItem {
	public UUID getSourceUUID();
	public void sendStack(ItemStack stack);
	public void sendStack(ItemStack stack, UUID destination);
	public void sendStack(ItemStack stack, UUID destination, ItemSendMode mode);
}
