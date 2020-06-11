package logisticspipes.proxy.buildcraft;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import buildcraft.api.tools.IToolWrench;

import logisticspipes.api.ILPPipeConfigTool;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.proxy.interfaces.ILPPipeConfigToolWrapper;

public class BuildCraftToolWrench implements ILPPipeConfigToolWrapper {

	@Override
	public ILPPipeConfigTool getWrappedTool(@Nonnull ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
			return new ILPPipeConfigTool() {

				@Override
				public boolean canWrench(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe) {
					if (wrench.isEmpty() || !(wrench.getItem() instanceof IToolWrench)) return false;
					return ((IToolWrench) wrench.getItem()).canWrench(player, EnumHand.MAIN_HAND, wrench, null);
				}

				@Override
				public void wrenchUsed(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe) {
					if (wrench.isEmpty() || !(wrench.getItem() instanceof IToolWrench)) return;
					((IToolWrench) wrench.getItem()).wrenchUsed(player, EnumHand.MAIN_HAND, wrench, null);
				}
			};
		}
		return null;
	}
}
