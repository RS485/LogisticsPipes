package logisticspipes.network.packets.pipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.Integer2ModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

@Accessors(chain = true)
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
		if (MainProxy.isClient(player.worldObj)) {
			module.defineFluidAmount(getInteger(), getInteger2());
		} else {
			module.changeFluidAmount(getInteger(), getInteger2(), player);
		}
	}
}
