package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ItemSinkImportPacket extends ModuleCoordinatesPacket {

	public ItemSinkImportPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkImportPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player, ModuleItemSink.class);
		if (module == null) {
			return;
		}
		module.importFromInventory();
	}
}
