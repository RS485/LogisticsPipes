package logisticspipes.proxy.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import logisticspipes.api.ILPPipeConfigTool;

public interface ILPPipeConfigToolWrapper {

	ILPPipeConfigTool getWrappedTool(@Nonnull ItemStack stack);
}
