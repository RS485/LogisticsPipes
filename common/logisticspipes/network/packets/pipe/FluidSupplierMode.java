package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
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
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(MainProxy.isClient(player.worldObj)) {
			if(pipe.pipe instanceof PipeItemsFluidSupplier && pipe.pipe instanceof PipeItemsFluidSupplier) {
				((PipeItemsFluidSupplier)pipe.pipe).setRequestingPartials((getInteger() % 10) == 1);
			}
			if(pipe.pipe instanceof PipeFluidSupplierMk2 && pipe.pipe instanceof PipeFluidSupplierMk2) {
				((PipeFluidSupplierMk2)pipe.pipe).setRequestingPartials((getInteger() % 10) == 1);
			}
		} else {
			if(pipe.pipe instanceof PipeItemsFluidSupplier) {
				PipeItemsFluidSupplier liquid = (PipeItemsFluidSupplier) pipe.pipe;
				((PipeItemsFluidSupplier)liquid).setRequestingPartials((getInteger() % 10) == 1);
			}
			if(pipe.pipe instanceof PipeFluidSupplierMk2) {
				PipeFluidSupplierMk2 liquid = (PipeFluidSupplierMk2) pipe.pipe;
				((PipeFluidSupplierMk2)liquid).setRequestingPartials((getInteger() % 10) == 1);
			}
		}
	}
}

