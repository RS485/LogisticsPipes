package logisticspipes.network.packets.pipe;

import logisticspipes.logic.LogicLiquidSupplierMk2;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class LiquidSupplierAmount extends IntegerCoordinatesPacket {

	public LiquidSupplierAmount(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new LiquidSupplierAmount(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getTile(player.worldObj, TileGenericPipe.class);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe.logic instanceof LogicLiquidSupplierMk2)) {
			return;
		}
		if(MainProxy.isClient(player.worldObj)) {
			((LogicLiquidSupplierMk2) pipe.pipe.logic).setAmount(getInteger());
		} else {
			((LogicLiquidSupplierMk2) pipe.pipe.logic).changeLiquidAmount(getInteger(), player);
		}
	}
}

