package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ModuleInventory extends InventoryModuleCoordinatesPacket {

	public ModuleInventory(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ModuleInventory(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (getIdentList() == null) return;
		IModuleInventoryReceive module = this.getLogisticsModule(player, IModuleInventoryReceive.class);
		if (module == null) {
			return;
		}
		module.handleInvContent(getIdentList());
	}
}
