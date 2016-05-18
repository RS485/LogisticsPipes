package logisticspipes.network.packets.pipe;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.PipeFluidTransportLogistics;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PipeFluidUpdate extends CoordinatesPacket {

	@Getter(value = AccessLevel.PRIVATE)
	@Setter
	private FluidStack[] renderCache = new FluidStack[ForgeDirection.values().length];
	private BitSet bits = new BitSet();

	public PipeFluidUpdate(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		bits = input.readBitSet();
		for (int i = 0; i < renderCache.length; i++) {
			if (bits.get(i)) {
				renderCache[i] = new FluidStack(FluidRegistry.getFluid(input.readInt()), input.readInt(), input.readNBTTagCompound());
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		for (int i = 0; i < renderCache.length; i++) {
			bits.set(i, renderCache[i] != null);
		}
		output.writeBitSet(bits);
		for (FluidStack aRenderCache : renderCache) {
			if (aRenderCache != null) {
				output.writeInt(FluidRegistry.getFluidID(aRenderCache.getFluid()));
				output.writeInt(aRenderCache.amount);
				output.writeNBTTagCompound(aRenderCache.tag);
			}
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null || pipe.pipe == null) {
			return;
		}
		if (!(pipe.pipe.transport instanceof PipeFluidTransportLogistics)) {
			return;
		}
		((PipeFluidTransportLogistics) pipe.pipe.transport).renderCache = renderCache;
	}

	@Override
	public ModernPacket template() {
		return new PipeFluidUpdate(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
