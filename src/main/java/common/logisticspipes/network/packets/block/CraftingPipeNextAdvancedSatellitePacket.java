package logisticspipes.network.packets.block;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class CraftingPipeNextAdvancedSatellitePacket extends IntegerCoordinatesPacket {

	public CraftingPipeNextAdvancedSatellitePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeNextAdvancedSatellitePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(!(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		((PipeItemsCraftingLogistics) pipe.pipe).setNextSatellite(player, getInteger());
	}
}

