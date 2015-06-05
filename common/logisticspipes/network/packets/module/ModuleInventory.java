package logisticspipes.network.packets.module;

import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

@Accessors(chain = true)
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
		IModuleInventoryReceive module = this.getLogisticsModule(player, IModuleInventoryReceive.class);
		if (module == null) {
			return;
		}
		module.handleInvContent(getIdentList());
	}
}
