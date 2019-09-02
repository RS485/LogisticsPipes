package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
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
				player.sendMessage(new TextComponentString("Debug enabled on Server"));
			} else {
				player.sendMessage(new TextComponentString("Debug disabled on Server"));
			}
		}
	}

	@Override
	public ModernPacket template() {
		return new PipeDebugResponse(getId());
	}
}
