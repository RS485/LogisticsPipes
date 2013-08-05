package logisticspipes.network.packets.pipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.BitSetHelper;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.transport.PipeFluidTransportLogistics;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class PipeFluidUpdate extends CoordinatesPacket {

	public PipeFluidUpdate(int id) {
		super(id);
	}

	@Getter(value=AccessLevel.PRIVATE)
	@Setter
	private FluidStack[] renderCache = new FluidStack[ForgeDirection.values().length];

	@Getter(value=AccessLevel.PRIVATE)
	@Setter
	private BitSet delta;

	@Getter(value=AccessLevel.PRIVATE)
	@Setter(value=AccessLevel.PRIVATE)
	private DataInputStream dataStream;
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		delta = BitSetHelper.read(data);
		this.setDataStream(data);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		BitSetHelper.write(data, delta);

		for (ForgeDirection dir : ForgeDirection.values()) {
			FluidStack liquid = renderCache[dir.ordinal()];

			if (delta.get(dir.ordinal() * 3 + 0)) {
				if (liquid != null) {
					data.writeShort(liquid.fluidID);
				} else {
					data.writeShort(0);
				}
			}
			//FIXME:Handle NBT
			/*
			if (delta.get(dir.ordinal() * 3 + 1)) {
				if (liquid != null) {
					data.writeShort(liquid.);
				} else {
					data.writeShort(0);
				}
			}*/
			if (delta.get(dir.ordinal() * 3 + 2)) {
				if (liquid != null) {
					data.writeShort(liquid.amount);
				} else {
					data.writeShort(0);
				}
			}
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null || pipe.pipe == null) return;
		if (!(pipe.pipe.transport instanceof PipeFluidTransportLogistics)) return;
		renderCache = ((PipeFluidTransportLogistics) pipe.pipe.transport).renderCache;
		try {
			for (ForgeDirection dir : ForgeDirection.values()) {
				if (renderCache[dir.ordinal()] == null) {
					renderCache[dir.ordinal()] = new FluidStack(0, 0);
				}
				
				if (delta.get(dir.ordinal() * 3 + 0)) {
					//FIXME:handle NBT
					renderCache[dir.ordinal()]=new FluidStack(getDataStream().readShort(),renderCache[dir.ordinal()].amount);
				}
				if (delta.get(dir.ordinal() * 3 + 1)) {
					//FIXME:handle NBT
					renderCache[dir.ordinal()]= new FluidStack(renderCache[dir.ordinal()].fluidID, renderCache[dir.ordinal()].amount);
				}
				if (delta.get(dir.ordinal() * 3 + 2)) {
					if(dir != ForgeDirection.UNKNOWN) {
						renderCache[dir.ordinal()].amount = Math.min(((PipeFluidTransportLogistics) pipe.pipe.transport).getSideCapacity(), getDataStream().readShort());
					} else {
						renderCache[dir.ordinal()].amount = Math.min(((PipeFluidTransportLogistics) pipe.pipe.transport).getInnerCapacity(), getDataStream().readShort());
					}
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
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
