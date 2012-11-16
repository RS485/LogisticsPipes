package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.ServerRouter;
import net.minecraftforge.common.ForgeDirection;

public class PacketRouterInformation extends PacketCoordinates {

	public HashMap<UUID, ExitRoute> _adjacent = new HashMap<UUID, ExitRoute>();
	public UUID uuid = UUID.randomUUID();
	public int _dimension;
	public boolean[] routedExit = new boolean[6];
	
	public PacketRouterInformation(int id,int x, int y, int z, int dimension, ServerRouter router) {
		super(id, x, y, z);
		for(RoutedPipe pipe:router._adjacent.keySet()) {
			_adjacent.put(pipe.getRouter().getId(), router._adjacent.get(pipe));
		}
		uuid = router.getId();
		this._dimension = dimension;
		for(int i=0;i<6;i++) {
			routedExit[i] = router.isRoutedExit(ForgeDirection.values()[i]);
		}
	}

	public PacketRouterInformation() {
		super();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(_dimension);
		data.writeUTF(uuid.toString());
		for(UUID id:_adjacent.keySet()) {
			data.writeBoolean(true);
			data.writeLong(id.getMostSignificantBits());
			data.writeLong(id.getLeastSignificantBits());
			data.writeByte(_adjacent.get(id).exitOrientation.ordinal());
			data.writeInt(_adjacent.get(id).metric);
			data.writeBoolean(_adjacent.get(id).isPipeLess);
		}
		data.writeBoolean(false);
		for(int i=0;i<6;i++) {
			data.writeBoolean(routedExit[i]);
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		_dimension = data.readInt();
		uuid = UUID.fromString(data.readUTF());
		while(data.readBoolean()) {
			UUID id = new UUID(data.readLong(), data.readLong());
			_adjacent.put(id, new ExitRoute(ForgeDirection.values()[data.readByte()],data.readInt(), data.readBoolean()));
		}
		for(int i=0;i<6;i++) {
			routedExit[i] = data.readBoolean();
		}
	}
}
