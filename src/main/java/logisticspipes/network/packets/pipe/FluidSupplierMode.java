package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class FluidSupplierMode extends IntegerCoordinatesPacket {

	public FluidSupplierMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidSupplierMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
		if (pipe == null) {
			return;
		}
		if (MainProxy.isClient(player.world)) {
			if (pipe.pipe instanceof PipeItemsFluidSupplier) {
				((PipeItemsFluidSupplier) pipe.pipe).setRequestingPartials((getInteger() % 10) == 1);
			}
			if (pipe.pipe instanceof PipeFluidSupplierMk2) {
				((PipeFluidSupplierMk2) pipe.pipe).setRequestingPartials((getInteger() % 10) == 1);
			}
		} else {
			if (pipe.pipe instanceof PipeItemsFluidSupplier) {
				PipeItemsFluidSupplier liquid = (PipeItemsFluidSupplier) pipe.pipe;
				liquid.setRequestingPartials((getInteger() % 10) == 1);
			}
			if (pipe.pipe instanceof PipeFluidSupplierMk2) {
				PipeFluidSupplierMk2 liquid = (PipeFluidSupplierMk2) pipe.pipe;
				liquid.setRequestingPartials((getInteger() % 10) == 1);
			}
		}
	}
}
