package logisticspipes.network.packets.block;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class CraftingPipePrevAdvancedSatellitePacket extends IntegerCoordinatesPacket {

	public CraftingPipePrevAdvancedSatellitePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipePrevAdvancedSatellitePacket(getId());
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
		((BaseLogicCrafting) pipe.pipe.logic).setPrevSatellite(player, getInteger());
	}
}

