package logisticspipes.network.packets.orderer;

import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

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
		final LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof IOrderManagerContentReceiver) {
			((IOrderManagerContentReceiver)tile.pipe).setOrderManagerContent(getIdentList());
		}
	}
}

