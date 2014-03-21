package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.pipe.PipeUpdate;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

public class RequestPipeUpdatePacket extends CoordinatesPacket {

	public RequestPipeUpdatePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestPipeUpdatePacket(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeUpdate.class).setPayload(((CoreRoutedPipe) pipe.pipe).getLogisticsNetworkPacket()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
		((CoreRoutedPipe) pipe.pipe).refreshRender(true);
	}
}

