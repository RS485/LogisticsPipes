package logisticspipes.network.packets;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.packets.abstracts.CoordinatesPacket;
import logisticspipes.network.packets.abstracts.ModernPacket;

public class CPipeNextSatellite extends CoordinatesPacket {

	public CPipeNextSatellite(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeNextSatellite(getID());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}
	
}
