package logisticspipes.network.packets.pipe;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class FluidCraftingPipeAdvancedSatelliteNextPacket extends IntegerCoordinatesPacket {

	public FluidCraftingPipeAdvancedSatelliteNextPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FluidCraftingPipeAdvancedSatelliteNextPacket(getId());
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
		((BaseLogicCrafting) pipe.pipe.logic).setNextFluidSatellite(player, getInteger());
	}
}

