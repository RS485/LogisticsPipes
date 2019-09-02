package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.Integer2ModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class FluidCraftingAmount extends Integer2ModuleCoordinatesPacket {

	public FluidCraftingAmount(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidCraftingAmount(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		if (MainProxy.isClient(player.world)) {
			module.defineFluidAmount(getInteger(), getInteger2());
		} else {
			module.changeFluidAmount(getInteger(), getInteger2(), player);
		}
	}
}
