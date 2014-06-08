package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class CraftingPipePriorityUpPacket extends CoordinatesPacket {

	public CraftingPipePriorityUpPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipePriorityUpPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		((PipeItemsCraftingLogistics) pipe.pipe).getLogisticsModule().priorityUp(player);
	}
}

