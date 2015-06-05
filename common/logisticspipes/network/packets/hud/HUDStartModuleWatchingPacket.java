package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

public class HUDStartModuleWatchingPacket extends ModuleCoordinatesPacket {

	public HUDStartModuleWatchingPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDStartModuleWatchingPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IModuleWatchReciver handler = this.getLogisticsModule(player, IModuleWatchReciver.class);
		if (handler == null) {
			return;
		}
		handler.startWatching(player);
	}
}
