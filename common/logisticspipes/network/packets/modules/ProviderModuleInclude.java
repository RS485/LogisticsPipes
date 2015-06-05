package logisticspipes.network.packets.modules;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.abstractpackets.BooleanModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class ProviderModuleInclude extends BooleanModuleCoordinatesPacket {

	public ProviderModuleInclude(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderModuleInclude(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleProvider module = this.getLogisticsModule(player, ModuleProvider.class);
		if (module == null) {
			return;
		}
		module.setFilterExcluded(isFlag());
	}
}
