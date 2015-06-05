package logisticspipes.network.packets.modules;

import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.QuickSortChestMarkerStorage;

import net.minecraft.entity.player.EntityPlayer;

public class QuickSortState extends Integer2CoordinatesPacket {

	public QuickSortState(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		QuickSortChestMarkerStorage.getInstance().setSlots(getPosX(), getPosY(), getPosZ(), getInteger(), getInteger2());
	}

	@Override
	public ModernPacket template() {
		return new QuickSortState(getId());
	}
}
