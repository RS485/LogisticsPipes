package logisticspipes.network.packets.orderer;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class OrdererManagerContent extends InventoryModuleCoordinatesPacket {

	public OrdererManagerContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new OrdererManagerContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe tile = this.getPipe(player.world);
		if (tile == null) {
			return;
		}
		if (tile.pipe instanceof IOrderManagerContentReceiver) {
			((IOrderManagerContentReceiver) tile.pipe).setOrderManagerContent(getIdentList());
		}
	}
}
