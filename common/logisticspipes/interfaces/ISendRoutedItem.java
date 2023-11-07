package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.tuples.Pair;

public interface ISendRoutedItem {

	int getSourceID();

	@Nonnull
	IRouter getRouter();

	IRoutedItem sendStack(@Nonnull ItemStack stack, Pair<Integer, SinkReply> reply, ItemSendMode mode, @Nonnull EnumFacing direction);

	IRoutedItem sendStack(@Nonnull ItemStack stack, int destination, ItemSendMode mode, IAdditionalTargetInformation info, @Nonnull EnumFacing direction);

	default IRoutedItem sendStack(@Nonnull ItemStack stack, int destRouterId, @Nonnull SinkReply sinkReply, @Nonnull ItemSendMode itemSendMode, @Nonnull EnumFacing direction) {
		return sendStack(stack, new Pair<>(destRouterId, sinkReply), itemSendMode, direction);
	}
}
