package logisticspipes.network.packets.module;

import cpw.mods.fml.common.network.Player;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.oldpackets.PacketPipeUpdate;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

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
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE, getPosX(), getPosY(), getPosZ(), ((CoreRoutedPipe) pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) player);
		((CoreRoutedPipe) pipe.pipe).refreshRender(true);
	}
}

