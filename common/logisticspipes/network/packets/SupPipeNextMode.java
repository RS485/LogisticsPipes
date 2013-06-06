package logisticspipes.network.packets;

import cpw.mods.fml.common.network.Player;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayerMP;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;
import logisticspipes.network.packets.old.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.proxy.MainProxy;

public class SupPipeNextMode extends CoordinatesPacket {

	public SupPipeNextMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SupPipeNextMode(getID());
	}

	@Override
	public void processPacket(EntityPlayerMP player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSupplier)) {
			return;
		}
		final LogicSupplier logic = (LogicSupplier) pipe.pipe.logic;
		logic.setRequestingPartials(!logic.isRequestingPartials());
		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, getPosX(), getPosY(), getPosZ(), logic.isRequestingPartials() ? 1 : 0).getPacket(), (Player)player);
	}
	
}
