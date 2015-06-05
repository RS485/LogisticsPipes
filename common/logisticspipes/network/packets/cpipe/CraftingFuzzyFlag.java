package logisticspipes.network.packets.cpipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.Integer2ModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CraftingFuzzyFlag extends Integer2ModuleCoordinatesPacket {

	public CraftingFuzzyFlag(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.setFuzzyCraftingFlag(getInteger(), getInteger2(), player);
	}

	@Override
	public ModernPacket template() {
		return new CraftingFuzzyFlag(getId());
	}

}
