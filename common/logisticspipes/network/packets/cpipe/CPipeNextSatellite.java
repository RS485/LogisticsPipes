package logisticspipes.network.packets.cpipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CPipeNextSatellite extends ModuleCoordinatesPacket {

	public CPipeNextSatellite(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeNextSatellite(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.setNextSatellite(player);
	}

}
