package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.packets.modules.ProviderModuleMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ProviderModuleNextModePacket extends ModuleCoordinatesPacket {

	public ProviderModuleNextModePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderModuleNextModePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleProvider module = this.getLogisticsModule(player, ModuleProvider.class);
		if (module == null) {
			return;
		}
		module.nextExtractionMode();
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderModuleMode.class).setMode(module.getExtractionMode().ordinal()).setModulePos(module), player);
	}
}
