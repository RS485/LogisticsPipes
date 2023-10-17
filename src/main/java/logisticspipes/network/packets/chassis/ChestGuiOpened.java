package logisticspipes.network.packets.chassis;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.LogisticsEventListener;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.module.AsyncQuicksortModule;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ChestGuiOpened extends ModernPacket {

	public ChestGuiOpened(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void processPacket(EntityPlayer player) {
		List<WeakReference<AsyncQuicksortModule>> list = LogisticsEventListener.chestQuickSortConnection.get(player);
		if (list == null || list.isEmpty()) {
			return;
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(EnableQuickSortMarker.class), player);
		for (WeakReference<AsyncQuicksortModule> sorter : list) {
			AsyncQuicksortModule module = sorter.get();
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
