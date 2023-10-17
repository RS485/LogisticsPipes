package logisticspipes.network.packets.cpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class CraftingPipeOpenConnectedGuiPacket extends ModuleCoordinatesPacket {

	public CraftingPipeOpenConnectedGuiPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeOpenConnectedGuiPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.openAttachedGui(player);
	}
}
