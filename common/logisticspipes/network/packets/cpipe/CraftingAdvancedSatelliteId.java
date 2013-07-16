package logisticspipes.network.packets.cpipe;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class CraftingAdvancedSatelliteId extends Integer2CoordinatesPacket {

	public CraftingAdvancedSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingAdvancedSatelliteId(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}
		((BaseLogicCrafting) pipe.pipe.logic).setSatelliteId(getInteger(), getInteger2());
	}
}

