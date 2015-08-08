package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class PipeDebugResponse extends CoordinatesPacket {

	public PipeDebugResponse(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe != null && pipe.isInitialized()) {
			pipe.pipe.debug.debugThisPipe = !pipe.pipe.debug.debugThisPipe;
			if (pipe.pipe.debug.debugThisPipe) {
				player.addChatComponentMessage(new ChatComponentText("Debug enabled on Server"));
			} else {
				player.addChatComponentMessage(new ChatComponentText("Debug disabled on Server"));
			}
		}
	}

	@Override
	public ModernPacket template() {
		return new PipeDebugResponse(getId());
	}
}
