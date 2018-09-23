package logisticspipes.proxy.buildcraft.subproxies;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.PipeApi;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public class BCPipeCapabilityProvider implements IBCPipeCapabilityProvider, IInjectable {

	private final LogisticsTileGenericPipe logsitcsPipe;

	public BCPipeCapabilityProvider(LogisticsTileGenericPipe logsitcsPipe) {
		this.logsitcsPipe = logsitcsPipe;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == PipeApi.CAP_INJECTABLE;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability == PipeApi.CAP_INJECTABLE) {
			return (T) this;
		}
		return null;
	}

	@Override
	public boolean canInjectItems(EnumFacing from) {
		return true;
	}

	@Nonnull
	@Override
	public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, EnumFacing from, EnumDyeColor color, double speed) {
		return logsitcsPipe.insertItem(from, stack);
	}
}
