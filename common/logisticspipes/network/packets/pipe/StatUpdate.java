package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class StatUpdate extends CoordinatesPacket {

	@Setter
	private CoreRoutedPipe pipe;
	private int stat_session_sent;
	private int stat_session_recieved;
	private int stat_session_relayed;
	private long stat_lifetime_sent;
	private long stat_lifetime_recieved;
	private long stat_lifetime_relayed;
	private int server_routing_table_size;
	
	public StatUpdate(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new StatUpdate(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe.pipe;
		cPipe.stat_session_sent = stat_session_sent;
		cPipe.stat_session_recieved = stat_session_recieved;
		cPipe.stat_session_relayed = stat_session_relayed;
		cPipe.stat_lifetime_sent = stat_lifetime_sent;
		cPipe.stat_lifetime_recieved = stat_lifetime_recieved;
		cPipe.stat_lifetime_relayed = stat_lifetime_relayed;
		cPipe.server_routing_table_size = server_routing_table_size;
	}
}

