package logisticspipes.network.packets.orderer;

import logisticspipes.network.abstractpackets.ItemPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class DiscContent extends ItemPacket {

	public DiscContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DiscContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)tile.pipe).setDisk(getStack());
		}
	}
}

