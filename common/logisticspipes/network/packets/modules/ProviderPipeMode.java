package logisticspipes.network.packets.modules;

import logisticspipes.logic.LogicProvider;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

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
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe.logic instanceof LogicProvider)) {
			return;
		}
		((LogicProvider) pipe.pipe.logic).setExtractionMode(getInteger());
	}
}

