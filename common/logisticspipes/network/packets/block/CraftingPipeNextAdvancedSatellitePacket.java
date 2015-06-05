package logisticspipes.network.packets.block;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.IntegerModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CraftingPipeNextAdvancedSatellitePacket extends IntegerModuleCoordinatesPacket {

	public CraftingPipeNextAdvancedSatellitePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeNextAdvancedSatellitePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.setNextSatellite(player, getInteger());
	}
}
