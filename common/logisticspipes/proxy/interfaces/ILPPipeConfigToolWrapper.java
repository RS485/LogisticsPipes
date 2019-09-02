package logisticspipes.proxy.interfaces;

import net.minecraft.item.ItemStack;

import logisticspipes.api.ILPPipeConfigTool;

public interface ILPPipeConfigToolWrapper {

	ILPPipeConfigTool getWrappedTool(ItemStack stack);
}
