package logisticspipes.network.packets.cpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class CPipeSatelliteImportBack extends InventoryModuleCoordinatesPacket {

	public CPipeSatelliteImportBack(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeSatelliteImportBack(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		for (int i = 0; i < getStackList().size(); i++) {
			module.dummyInventory.setInventorySlotContents(i, getStackList().get(i));
		}
	}
}
