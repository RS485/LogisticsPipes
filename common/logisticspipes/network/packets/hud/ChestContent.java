package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class ChestContent extends InventoryCoordinatesPacket {

	public ChestContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ChestContent(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof IChestContentReceiver) {
			((IChestContentReceiver) tile.pipe).setReceivedChestContent(getIdentList());
		}
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

