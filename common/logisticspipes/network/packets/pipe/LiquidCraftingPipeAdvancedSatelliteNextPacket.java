package logisticspipes.network.packets.pipe;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class LiquidCraftingPipeAdvancedSatelliteNextPacket extends IntegerCoordinatesPacket {

	public LiquidCraftingPipeAdvancedSatelliteNextPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new LiquidCraftingPipeAdvancedSatelliteNextPacket(getId());
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
		((BaseLogicCrafting) pipe.pipe.logic).setNextLiquidSatellite(player, getInteger());
	}
}

