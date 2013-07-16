package logisticspipes.network.packets.orderer;

import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class OrdererManagerContent extends InventoryCoordinatesPacket {

	public OrdererManagerContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new OrdererManagerContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof IOrderManagerContentReceiver) {
			((IOrderManagerContentReceiver)tile.pipe).setOrderManagerContent(getIdentList());
		}
	}
}

