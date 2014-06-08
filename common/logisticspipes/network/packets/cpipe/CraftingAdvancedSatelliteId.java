package logisticspipes.network.packets.cpipe;

import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class CraftingAdvancedSatelliteId extends Integer2CoordinatesPacket {

	public CraftingAdvancedSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingAdvancedSatelliteId(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		((PipeItemsCraftingLogistics) pipe.pipe).getLogisticsModule().setSatelliteId(getInteger(), getInteger2());
	}
}

