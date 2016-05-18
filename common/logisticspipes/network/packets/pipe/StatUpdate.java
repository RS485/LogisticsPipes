package logisticspipes.network.packets.pipe;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.ExitRoute;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
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
		int numentries = 0;
		for (List<ExitRoute> route : pipe.getRouter().getRouteTable()) {
			if (route != null && !route.isEmpty()) {
				++numentries;
			}
		}
		server_routing_table_size = numentries;
	}

	@Override
	public void writeData(LPDataOutput output) {
		initData();
		super.writeData(output);
		output.writeInt(stat_session_sent);
		output.writeInt(stat_session_recieved);
		output.writeInt(stat_session_relayed);
		output.writeLong(stat_lifetime_sent);
		output.writeLong(stat_lifetime_recieved);
		output.writeLong(stat_lifetime_relayed);
		output.writeInt(server_routing_table_size);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		stat_session_sent = input.readInt();
		stat_session_recieved = input.readInt();
		stat_session_relayed = input.readInt();
		stat_lifetime_sent = input.readLong();
		stat_lifetime_recieved = input.readLong();
		stat_lifetime_relayed = input.readLong();
		server_routing_table_size = input.readInt();
	}
}
