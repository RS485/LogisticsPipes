package logisticspipes.network.packets.pipe;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class LiquidCraftingAmount extends Integer2CoordinatesPacket {

	public LiquidCraftingAmount(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new LiquidCraftingAmount(getId());
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
			((BaseLogicCrafting) pipe.pipe.logic).defineLiquidAmount(getInteger(), getInteger2());
		} else {
			((BaseLogicCrafting) pipe.pipe.logic).changeLiquidAmount(getInteger(), getInteger2(), player);
		}
	}
}

