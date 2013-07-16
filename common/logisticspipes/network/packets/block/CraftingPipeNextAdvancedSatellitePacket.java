package logisticspipes.network.packets.block;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class CraftingPipeNextAdvancedSatellitePacket extends IntegerCoordinatesPacket {

	public CraftingPipeNextAdvancedSatellitePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeNextAdvancedSatellitePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}
		((BaseLogicCrafting) pipe.pipe.logic).setNextSatellite(player, getInteger());
	}
}

