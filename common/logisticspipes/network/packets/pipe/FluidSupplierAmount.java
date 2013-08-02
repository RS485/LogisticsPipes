package logisticspipes.network.packets.pipe;

import logisticspipes.logic.LogicFluidSupplierMk2;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class FluidSupplierAmount extends IntegerCoordinatesPacket {

	public FluidSupplierAmount(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidSupplierAmount(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getTile(player.worldObj, TileGenericPipe.class);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe.logic instanceof LogicFluidSupplierMk2)) {
			return;
		}
		if(MainProxy.isClient(player.worldObj)) {
			((LogicFluidSupplierMk2) pipe.pipe.logic).setAmount(getInteger());
		} else {
			((LogicFluidSupplierMk2) pipe.pipe.logic).changeFluidAmount(getInteger(), player);
		}
	}
}

