package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.IRouter;
import net.minecraft.item.ItemStack;

public interface ISendRoutedItem {
	public int getSourceID();
	public IRouter getRouter();
	public void sendStack(ItemStack stack);
	public void sendStack(ItemStack stack, int destination, List<IRelayItem> relays);
	public void sendStack(ItemStack stack, int destination, ItemSendMode mode, List<IRelayItem> relays);
}
