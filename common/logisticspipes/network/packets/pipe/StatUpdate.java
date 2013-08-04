package logisticspipes.network.packets.pipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

	private void initData() {
		setPosX(pipe.getX());
		setPosY(pipe.getY());
		setPosZ(pipe.getZ());
		stat_session_sent = pipe.stat_session_sent;
		stat_session_recieved = pipe.stat_session_recieved;
		stat_session_relayed = pipe.stat_session_relayed;
		stat_lifetime_sent = pipe.stat_lifetime_sent;
		stat_lifetime_recieved = pipe.stat_lifetime_recieved;
		stat_lifetime_relayed = pipe.stat_lifetime_relayed;
		server_routing_table_size = pipe.getRouter().getRouteTable().size();
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		initData();
		super.writeData(data);
		data.writeInt(stat_session_sent);
		data.writeInt(stat_session_recieved);
		data.writeInt(stat_session_relayed);
		data.writeLong(stat_lifetime_sent);
		data.writeLong(stat_lifetime_recieved);
		data.writeLong(stat_lifetime_relayed);
		data.writeInt(server_routing_table_size);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		stat_session_sent = data.readInt();
		stat_session_recieved = data.readInt();
		stat_session_relayed = data.readInt();
		stat_lifetime_sent = data.readLong();
		stat_lifetime_recieved = data.readLong();
		stat_lifetime_relayed = data.readLong();
		server_routing_table_size = data.readInt();
	}
}

