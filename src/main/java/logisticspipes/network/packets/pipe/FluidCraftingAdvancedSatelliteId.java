package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class FluidCraftingAdvancedSatelliteId extends Integer2CoordinatesPacket {

	public FluidCraftingAdvancedSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidCraftingAdvancedSatelliteId(getId());
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
		((PipeItemsCraftingLogistics) pipe.pipe).setFluidSatelliteId(getInteger(), getInteger2());
	}
}

