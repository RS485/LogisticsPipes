package logisticspipes.interfaces;

import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.IRouter;
import net.minecraft.item.ItemStack;

public interface ISendRoutedItem {
	public UUID getSourceUUID();
	public IRouter getRouter();
	public void sendStack(ItemStack stack);
	public void sendStack(ItemStack stack, UUID destination, List<IRelayItem> relays);
	public void sendStack(ItemStack stack, UUID destination, ItemSendMode mode, List<IRelayItem> relays);
}
