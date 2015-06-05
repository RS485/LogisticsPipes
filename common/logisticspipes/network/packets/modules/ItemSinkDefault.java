package logisticspipes.network.packets.modules;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractpackets.BooleanModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class ItemSinkDefault extends BooleanModuleCoordinatesPacket {

	public ItemSinkDefault(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkDefault(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player, ModuleItemSink.class);
		if (module == null) {
			return;
		}
		module.setDefaultRoute(isFlag());
	}
}
