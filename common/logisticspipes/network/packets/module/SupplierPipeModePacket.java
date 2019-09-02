package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleActiveSupplier.PatternMode;
import logisticspipes.modules.ModuleActiveSupplier.SupplyMode;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.packets.modules.SupplierPipeMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SupplierPipeModePacket extends ModuleCoordinatesPacket {

	public SupplierPipeModePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SupplierPipeModePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleActiveSupplier module = this.getLogisticsModule(player, ModuleActiveSupplier.class);
		if (module == null) {
			return;
		}
		int mode;
		if (module.hasPatternUpgrade()) {
			mode = module.getPatternMode().ordinal() + 1;
			if (mode >= PatternMode.values().length) {
				mode = 0;
			}
			module.setPatternMode(PatternMode.values()[mode]);
		} else {
			mode = module.getSupplyMode().ordinal() + 1;
			if (mode >= SupplyMode.values().length) {
				mode = 0;
			}
			module.setSupplyMode(SupplyMode.values()[mode]);
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeMode.class).setHasPatternUpgrade(module.hasPatternUpgrade()).setInteger(mode).setPacketPos(this), player);
	}
}
