package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeFluidSupplierMk2.MinMode;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class FluidSupplierMinMode extends IntegerCoordinatesPacket {

	public FluidSupplierMinMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidSupplierMinMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof PipeFluidSupplierMk2) {
			((PipeFluidSupplierMk2) pipe.pipe).setMinMode(MinMode.values()[getInteger()]);
		}
	}
}
