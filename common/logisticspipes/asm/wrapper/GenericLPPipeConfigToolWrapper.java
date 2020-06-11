package logisticspipes.asm.wrapper;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import logisticspipes.api.ILPPipeConfigTool;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.proxy.interfaces.ILPPipeConfigToolWrapper;

public class GenericLPPipeConfigToolWrapper extends AbstractWrapper implements ILPPipeConfigToolWrapper {

	private ILPPipeConfigToolWrapper wrapper;
	private final String name;

	GenericLPPipeConfigToolWrapper(ILPPipeConfigToolWrapper wrapper, String name) {
		this.wrapper = wrapper;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTypeName() {
		return "LPPipeConfigToolWrapper";
	}

	@Override
	public ILPPipeConfigTool getWrappedTool(@Nonnull ItemStack stack) {
		if (isEnabled()) {
			try {
				ILPPipeConfigTool tool = wrapper.getWrappedTool(stack);
				if (tool != null) {
					return new GenericLPPipeConfigTool(tool, this);
				}
			} catch (Exception | NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return null;
	}

	static class GenericLPPipeConfigTool extends AbstractSubWrapper implements ILPPipeConfigTool {

		ILPPipeConfigTool tool;

		public GenericLPPipeConfigTool(ILPPipeConfigTool tool, AbstractWrapper wrapper) {
			super(wrapper);
			this.tool = tool;
		}

		@Override
		public boolean canWrench(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe) {
			if (isEnabled()) {
				try {
					return tool.canWrench(player, wrench, pipe);
				} catch (Exception | NoClassDefFoundError e) {
					handleException(e);
				}
			}
			return false;
		}

		@Override
		public void wrenchUsed(EntityPlayer player, @Nonnull ItemStack wrench, ILPPipeTile pipe) {
			if (isEnabled()) {
				try {
					tool.wrenchUsed(player, wrench, pipe);
				} catch (Exception | NoClassDefFoundError e) {
					handleException(e);
				}
			}
		}
	}
}
