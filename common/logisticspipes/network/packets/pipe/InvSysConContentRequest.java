package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class InvSysConContentRequest extends CoordinatesPacket {

	public InvSysConContentRequest(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new InvSysConContentRequest(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof PipeItemsInvSysConnector) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(InvSysConContent.class).setIdentSet(((PipeItemsInvSysConnector) pipe.pipe).getExpectedItems()), player);
		}
	}
}
