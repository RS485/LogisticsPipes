package logisticspipes.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public interface ILPItemAcceptor {

	boolean accept(LogisticsTileGenericPipe pipe, EnumFacing from, ItemStack stack);
}
