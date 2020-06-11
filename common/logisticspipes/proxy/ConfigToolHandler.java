package logisticspipes.proxy;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import logisticspipes.api.ILPPipeConfigTool;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.proxy.buildcraft.BuildCraftToolWrench;
import logisticspipes.proxy.cofh.CoFHToolHammer;
import logisticspipes.proxy.interfaces.ILPPipeConfigToolWrapper;

public class ConfigToolHandler {

	public List<ILPPipeConfigToolWrapper> wrappers = new ArrayList<>();

	public boolean canWrench(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe) {
		if (wrench.isEmpty()) return false;
		if (wrench.getItem() instanceof ILPPipeConfigTool) {
			return ((ILPPipeConfigTool) wrench.getItem()).canWrench(player, wrench, pipe);
		}
		for (ILPPipeConfigToolWrapper wrapper : wrappers) {
			ILPPipeConfigTool wrapped = wrapper.getWrappedTool(wrench);
			if (wrapped != null) {
				return wrapped.canWrench(player, wrench, pipe);
			}
		}
		return false;
	}

	public void wrenchUsed(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe) {
		if (wrench.isEmpty()) return;
		if (wrench.getItem() instanceof ILPPipeConfigTool) {
			((ILPPipeConfigTool) wrench.getItem()).wrenchUsed(player, wrench, pipe);
			return;
		}
		for (ILPPipeConfigToolWrapper wrapper : wrappers) {
			ILPPipeConfigTool wrapped = wrapper.getWrappedTool(wrench);
			if (wrapped != null) {
				wrapped.wrenchUsed(player, wrench, pipe);
				return;
			}
		}
	}

	void registerWrapper() {
		wrappers.add(LogisticsWrapperHandler.getWrappedPipeConfigToolWrapper("buildcraft.api.tools.IToolWrench", "BuildCraft", BuildCraftToolWrench.class));
		wrappers.add(LogisticsWrapperHandler.getWrappedPipeConfigToolWrapper("cofh.api.item.IToolHammer", "CoFH", CoFHToolHammer.class));
	}
}
