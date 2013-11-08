package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.item.ItemStack;

public interface ISendRoutedItem {
	int getSourceID();
	IRouter getRouter();
	Pair<Integer, SinkReply> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude);
	void sendStack(ItemStack stack, Pair<Integer, SinkReply> reply, ItemSendMode mode);
	void sendStack(ItemStack stack, int destination, ItemSendMode mode);
}
