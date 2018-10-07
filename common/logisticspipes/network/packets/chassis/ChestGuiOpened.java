package logisticspipes.network.packets.chassis;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.LogisticsEventListener;
import logisticspipes.modules.ModuleQuickSort;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ChestGuiOpened extends ModernPacket {

	public ChestGuiOpened(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		List<WeakReference<ModuleQuickSort>> list = LogisticsEventListener.chestQuickSortConnection.get(player);
		if (list == null || list.isEmpty()) {
			return;
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(EnableQuickSortMarker.class), player);
		for (WeakReference<ModuleQuickSort> sorter : list) {
			ModuleQuickSort module = sorter.get();
			if (module == null) {
				continue;
			}
			module.addWatchingPlayer(player);
		}
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new ChestGuiOpened(getId());
	}
}
