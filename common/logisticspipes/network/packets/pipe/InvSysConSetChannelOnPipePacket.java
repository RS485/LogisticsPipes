package logisticspipes.network.packets.pipe;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringCoordinatesPacket;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class InvSysConSetChannelOnPipePacket extends StringCoordinatesPacket {

	public InvSysConSetChannelOnPipePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe.pipe instanceof PipeItemsInvSysConnector) {
			PipeItemsInvSysConnector conPipe = (PipeItemsInvSysConnector) pipe.pipe;
			conPipe.setChannelFromClient(UUID.fromString(getString()));
		}
	}

	@Override
	public ModernPacket template() {
		return new InvSysConSetChannelOnPipePacket(getId());
	}
}
