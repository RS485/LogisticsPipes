package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class ChassiePipeModuleContent extends InventoryCoordinatesPacket {

	public ChassiePipeModuleContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ChassiePipeModuleContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi) {
			PipeLogisticsChassi chassie = (PipeLogisticsChassi) pipe.pipe;
			chassie.handleModuleItemIdentifierList(getIdentList());
		}
	}
}

