package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ChassisPipeModuleContent extends InventoryModuleCoordinatesPacket {

	public ChassisPipeModuleContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ChassisPipeModuleContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.world, LTGPCompletionCheck.PIPE);
		if (pipe.pipe instanceof PipeLogisticsChassis) {
			PipeLogisticsChassis chassis = (PipeLogisticsChassis) pipe.pipe;
			chassis.handleModuleItemIdentifierList(getIdentList());
		}
	}
}
