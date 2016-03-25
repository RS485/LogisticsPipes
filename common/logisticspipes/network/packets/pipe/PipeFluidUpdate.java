package logisticspipes.network.packets.pipe;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.PipeFluidTransportLogistics;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PipeFluidUpdate extends CoordinatesPacket {

	public PipeFluidUpdate(int id) {
		super(id);
	}

	@Getter(value = AccessLevel.PRIVATE)
	@Setter
	private FluidStack[] renderCache = new FluidStack[ForgeDirection.values().length];

	private BitSet bits = new BitSet();

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		bits = data.readBitSet();
		for(int i=0;i < renderCache.length;i++) {
			if(bits.get(i)) {
				renderCache[i] = new FluidStack(FluidRegistry.getFluid(data.readInt()), data.readInt(), data.readNBTTagCompound());
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		for(int i=0;i < renderCache.length;i++) {
			bits.set(i, renderCache[i] != null);
		}
		data.writeBitSet(bits);
		for(int i=0;i < renderCache.length;i++) {
			if(renderCache[i] != null) {
				data.writeInt(FluidRegistry.getFluidID(renderCache[i].getFluid()));
				data.writeInt(renderCache[i].amount);
				data.writeNBTTagCompound(renderCache[i].tag);
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
