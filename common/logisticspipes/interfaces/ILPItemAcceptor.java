package logisticspipes.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public interface ILPItemAcceptor {

	boolean accept(LogisticsTileGenericPipe pipe, Direction from, ItemStack stack);
}
