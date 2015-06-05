package logisticspipes.network.packets.cpipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.Integer2ModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CraftingAdvancedSatelliteId extends Integer2ModuleCoordinatesPacket {

	public CraftingAdvancedSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingAdvancedSatelliteId(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.setSatelliteId(getInteger(), getInteger2());
	}
}
