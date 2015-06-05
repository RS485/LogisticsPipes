package logisticspipes.network.packets.pipe;

import logisticspipes.interfaces.ISendQueueContentRecieiver;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

public class SendQueueContent extends InventoryModuleCoordinatesPacket {

	public SendQueueContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SendQueueContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof ISendQueueContentRecieiver) {
			ISendQueueContentRecieiver receiver = (ISendQueueContentRecieiver) pipe.pipe;
			receiver.handleSendQueueItemIdentifierList(getIdentList());
		}
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
