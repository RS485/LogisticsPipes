package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.SupplierPipeMode;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class SupplierPipeModePacket extends CoordinatesPacket {

	public SupplierPipeModePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SupplierPipeModePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof PipeItemsSupplierLogistics)) {
			return;
		}
		final PipeItemsSupplierLogistics logic = (PipeItemsSupplierLogistics) pipe.pipe;
		logic.setRequestingPartials(!logic.isRequestingPartials());
//TODO	MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, getPosX(), getPosY(), getPosZ(), logic.isRequestingPartials() ? 1 : 0).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeMode.class).setInteger(logic.isRequestingPartials() ? 1 : 0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player)player);
	}
}

