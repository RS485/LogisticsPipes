package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

public class RequestSignPacket extends CoordinatesPacket {

	public RequestSignPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null) {
			return;
		}
		((CoreRoutedPipe) pipe.pipe).sendSignData(player);
	}

	@Override
	public ModernPacket template() {
		return new RequestSignPacket(getId());
	}
}
