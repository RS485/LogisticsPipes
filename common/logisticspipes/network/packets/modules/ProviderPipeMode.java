package logisticspipes.network.packets.modules;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

public class ProviderPipeMode extends IntegerCoordinatesPacket {

	public ProviderPipeMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderPipeMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		((PipeItemsProviderLogistics) pipe.pipe).setExtractionMode(getInteger());
	}
}
