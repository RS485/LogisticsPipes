package logisticspipes.interfaces;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;

public interface ISendRoutedItem {

	int getSourceID();

	@Nonnull
	IRouter getRouter();

	Pair<Integer, SinkReply> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude);

	IRoutedItem sendStack(ItemStack stack, Pair<Integer, SinkReply> reply, ItemSendMode mode);

	IRoutedItem sendStack(ItemStack stack, int destination, ItemSendMode mode, IAdditionalTargetInformation info);

	default IRoutedItem sendStack(@Nonnull ItemStack stack, int destRouterId, @Nonnull SinkReply sinkReply, @Nonnull CoreRoutedPipe.ItemSendMode itemSendMode) {
		return sendStack(stack, new Pair<>(destRouterId, sinkReply), itemSendMode);
	}
}
