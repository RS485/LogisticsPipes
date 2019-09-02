package logisticspipes.proxy.cofh;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cofh.api.item.IToolHammer;

import logisticspipes.api.ILPPipeConfigTool;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.proxy.interfaces.ILPPipeConfigToolWrapper;

public class CoFHToolHammer implements ILPPipeConfigToolWrapper {

	@Override
	public ILPPipeConfigTool getWrappedTool(ItemStack stack) {
		if (stack.getItem() instanceof IToolHammer) {
			return new ILPPipeConfigTool() {

				@Override
				public boolean canWrench(EntityPlayer player, ItemStack wrench, ILPPipeTile pipe) {
					if (wrench.isEmpty() || !(wrench.getItem() instanceof IToolHammer)) return false;
					return ((IToolHammer) wrench.getItem()).isUsable(wrench, player, pipe.getBlockPos());
				}

				@Override
				public void wrenchUsed(EntityPlayer player, ItemStack wrench, ILPPipeTile pipe) {
					if (wrench.isEmpty() || !(wrench.getItem() instanceof IToolHammer)) return;
					((IToolHammer) wrench.getItem()).toolUsed(wrench, player, pipe.getBlockPos());
				}
			};
		}
		return null;
	}
}
