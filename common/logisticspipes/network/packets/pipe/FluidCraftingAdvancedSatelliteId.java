package logisticspipes.network.packets.pipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.Integer2ModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class FluidCraftingAdvancedSatelliteId extends Integer2ModuleCoordinatesPacket {

	public FluidCraftingAdvancedSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidCraftingAdvancedSatelliteId(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.setFluidSatelliteId(getInteger(), getInteger2());
	}
}
