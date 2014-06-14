package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class ChestContent extends InventoryModuleCoordinatesPacket {

	public ChestContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ChestContent(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
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

