package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.network.abstractpackets.BooleanModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ElectricManagerPacket extends BooleanModuleCoordinatesPacket {

	public ElectricManagerPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ElectricManagerPacket(getId());
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
