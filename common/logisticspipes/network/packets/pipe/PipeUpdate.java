package logisticspipes.network.packets.pipe;

import logisticspipes.network.TilePacketWrapper;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.PacketPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class PipeUpdate extends CoordinatesPacket {

	@Getter
	@Setter
	private PacketPayload payload;
	
	public PipeUpdate(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PipeUpdate(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		if(tile.pipe == null) {
			return;
		}
		new TilePacketWrapper(new Class[] { TileGenericPipe.class, tile.pipe.transport.getClass(), tile.pipe.logic.getClass() }).fromPayload(new Object[] { tile.pipe.container, tile.pipe.transport, tile.pipe.logic }, getPayload());
	}
}

