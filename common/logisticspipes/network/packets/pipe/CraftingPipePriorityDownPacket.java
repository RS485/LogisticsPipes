package logisticspipes.network.packets.pipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CraftingPipePriorityDownPacket extends ModuleCoordinatesPacket {

	public CraftingPipePriorityDownPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipePriorityDownPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.priorityDown(player);
	}
}
