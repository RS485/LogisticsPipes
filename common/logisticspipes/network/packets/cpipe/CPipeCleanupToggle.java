package logisticspipes.network.packets.cpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class CPipeCleanupToggle extends ModuleCoordinatesPacket {

	public CPipeCleanupToggle(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeCleanupToggle(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.toogleCleaupMode();
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CPipeCleanupStatus.class).setMode(module.cleanupModeIsExclude).setPacketPos(this), player);
	}
}
