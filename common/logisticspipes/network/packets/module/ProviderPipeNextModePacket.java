package logisticspipes.network.packets.module;

import cpw.mods.fml.common.network.Player;
import logisticspipes.logic.LogicProvider;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.oldpackets.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class ProviderPipeNextModePacket extends CoordinatesPacket {

	public ProviderPipeNextModePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderPipeNextModePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		final LogicProvider logic = (LogicProvider) providerpipe.logic;
		logic.nextExtractionMode();
		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, getPosX(), getPosY(), getPosZ(), logic.getExtractionMode().ordinal()).getPacket(), (Player) player);
	}
}

