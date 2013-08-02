package logisticspipes.network.packets.pipe;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class FluidCraftingAmount extends Integer2CoordinatesPacket {

	public FluidCraftingAmount(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidCraftingAmount(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}
		if(MainProxy.isClient(player.worldObj)) {
			((BaseLogicCrafting) pipe.pipe.logic).defineFluidAmount(getInteger(), getInteger2());
		} else {
			((BaseLogicCrafting) pipe.pipe.logic).changeFluidAmount(getInteger(), getInteger2(), player);
		}
	}
}

