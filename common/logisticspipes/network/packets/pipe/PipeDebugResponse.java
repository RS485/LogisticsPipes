package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;

public class PipeDebugResponse extends CoordinatesPacket {
	
	public PipeDebugResponse(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.getEntityWorld());
		if(tile != null) {
			((CoreRoutedPipe) tile.pipe).debugThisPipe = !((CoreRoutedPipe) tile.pipe).debugThisPipe;
			if(((CoreRoutedPipe) tile.pipe).debugThisPipe) {
				player.sendChatToPlayer(ChatMessageComponent.createFromText("Debug enabled on Server"));
			} else {
				player.sendChatToPlayer(ChatMessageComponent.createFromText("Debug disabled on Server"));
			}
		}
	}
	
	@Override
	public ModernPacket template() {
		return new PipeDebugResponse(getId());
	}
}
