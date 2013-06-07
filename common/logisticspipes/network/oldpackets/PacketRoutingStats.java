package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.pipes.basic.CoreRoutedPipe;

public class PacketRoutingStats extends PacketCoordinates {

	private CoreRoutedPipe pipe;
	public int stat_session_sent;
	public int stat_session_recieved;
	public int stat_session_relayed;
	public long stat_lifetime_sent;
	public long stat_lifetime_recieved;
	public long stat_lifetime_relayed;
	public int server_routing_table_size;
	
	public PacketRoutingStats() {
		super();
	}
	
	public PacketRoutingStats(int id, CoreRoutedPipe pipe) {
		super(id, pipe.xCoord, pipe.yCoord, pipe.zCoord);
		this.pipe = pipe;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(pipe.stat_session_sent);
		data.writeInt(pipe.stat_session_recieved);
		data.writeInt(pipe.stat_session_relayed);
		data.writeLong(pipe.stat_lifetime_sent);
		data.writeLong(pipe.stat_lifetime_recieved);
		data.writeLong(pipe.stat_lifetime_relayed);
		data.writeInt(pipe.getRouter().getIRoutersByCost().size());
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
