package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.BitSetCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsFirewall;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class FireWallFlag extends BitSetCoordinatesPacket {

	public FireWallFlag(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new FireWallFlag(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsFirewall) {
			PipeItemsFirewall firewall = (PipeItemsFirewall) pipe.pipe;
			firewall.setFlags(getFlags());
		}
	}
}

