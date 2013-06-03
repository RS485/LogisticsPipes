package logisticspipes.network.packets;

import cpw.mods.fml.common.network.Player;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayerMP;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.LogicProvider;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;
import logisticspipes.network.packets.old.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.proxy.MainProxy;

public class ProvPipeNextMode extends CoordinatesPacket {

	public ProvPipeNextMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProvPipeNextMode(getID());
	}

	@Override
	public void processPacket(EntityPlayerMP player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		final LogicProvider logic = (LogicProvider)providerpipe.logic;
		logic.nextExtractionMode();
		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, getPosX(), getPosY(), getPosZ(), logic.getExtractionMode().ordinal()).getPacket(), (Player)player);

	}
	
}
