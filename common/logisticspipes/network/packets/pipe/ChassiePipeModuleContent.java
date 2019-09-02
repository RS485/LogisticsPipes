package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ChassiePipeModuleContent extends InventoryModuleCoordinatesPacket {

	public ChassiePipeModuleContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ChassiePipeModuleContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof PipeLogisticsChassi) {
			PipeLogisticsChassi chassie = (PipeLogisticsChassi) pipe.pipe;
			chassie.handleModuleItemIdentifierList(getIdentList());
		}
	}
}
