package logisticspipes.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public interface ILPItemAcceptor {

	boolean accept(LogisticsTileGenericPipe pipe, EnumFacing from, @Nonnull ItemStack stack);
}
