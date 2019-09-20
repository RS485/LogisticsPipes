package logisticspipes.interfaces;

import java.util.List;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.Router;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.tuples.Tuple2;

public interface ISendRoutedItem {

	int getSourceID();

	Router getRouter();

	Tuple2<Integer, SinkReply> hasDestination(ItemStack stack, boolean allowDefault, List<Integer> routerIDsToExclude);

	IRoutedItem sendStack(ItemStack stack, Tuple2<Integer, SinkReply> reply, ItemSendMode mode);

	IRoutedItem sendStack(ItemStack stack, int destination, ItemSendMode mode, IAdditionalTargetInformation info);
}
