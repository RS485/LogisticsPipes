package logisticspipes.network.packets.chassis;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.QuickSortChestMarkerStorage;

import net.minecraft.entity.player.EntityPlayer;

public class EnableQuickSortMarker extends ModernPacket {

	public EnableQuickSortMarker(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		QuickSortChestMarkerStorage.getInstance().enable();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new EnableQuickSortMarker(getId());
	}
}
