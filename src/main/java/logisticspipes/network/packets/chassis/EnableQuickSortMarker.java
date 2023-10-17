package logisticspipes.network.packets.chassis;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.QuickSortChestMarkerStorage;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class EnableQuickSortMarker extends ModernPacket {

	public EnableQuickSortMarker(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		QuickSortChestMarkerStorage.getInstance().enable();
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new EnableQuickSortMarker(getId());
	}
}
