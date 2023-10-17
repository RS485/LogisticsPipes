package logisticspipes.network.packets.orderer;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.interfaces.IRequestWatcher;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class OrderWatchRemovePacket extends IntegerCoordinatesPacket {

	public OrderWatchRemovePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.world);
		if (tile != null && tile.pipe instanceof IRequestWatcher) {
			((IRequestWatcher) tile.pipe).handleClientSideRemove(getInteger());
		}
	}

	@Override
	public ModernPacket template() {
		return new OrderWatchRemovePacket(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
