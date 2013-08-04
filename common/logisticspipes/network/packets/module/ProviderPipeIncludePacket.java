package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.ProviderPipeInclude;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class ProviderPipeIncludePacket extends CoordinatesPacket {

	public ProviderPipeIncludePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderPipeIncludePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerPipe = (PipeItemsProviderLogistics) pipe.pipe;
		providerPipe.setFilterExcluded(!providerPipe.isExcludeFilter());
//TODO	MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, getPosX(), getPosY(), getPosZ(), logic.isExcludeFilter() ? 1 : 0).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderPipeInclude.class).setInteger(providerPipe.isExcludeFilter() ? 1 : 0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player)player);
	}
}

