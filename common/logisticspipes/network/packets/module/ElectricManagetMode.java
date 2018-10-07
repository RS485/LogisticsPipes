package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.network.abstractpackets.BooleanModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ElectricManagetMode extends BooleanModuleCoordinatesPacket {

	public ElectricManagetMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ElectricManagetMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleElectricManager module = this.getLogisticsModule(player, ModuleElectricManager.class);
		if (module == null) {
			return;
		}
		module.setDischargeMode(isFlag());
	}
}
