package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import net.minecraft.item.ItemStack;

public interface ISendRoutedItem {
	int getSourceID();
	IRouter getRouter();
	Pair3<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude);
	void sendStack(ItemStack stack, Pair3<Integer, SinkReply, List<IFilter>> reply, ItemSendMode mode);
	void sendStack(ItemStack stack, int destination, ItemSendMode mode, List<IRelayItem> relays);
}
