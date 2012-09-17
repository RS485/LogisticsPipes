package logisticspipes.network.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;

import logisticspipes.routing.IRouter;
import logisticspipes.routing.ServerRouter;
import buildcraft.api.core.Orientations;

public class PacketRouterInformation extends PacketCoordinates {
	
	public HashMap<UUID, Orientations> _routeTable = new HashMap<UUID, Orientations>();
	public HashMap<UUID, Integer> _routeCosts = new HashMap<UUID, Integer>();
	public UUID uuid = UUID.randomUUID();
	public int _dimension;
	public boolean[] routedExit = new boolean[6];
	
	public PacketRouterInformation(int id,int x, int y, int z, int dimension, ServerRouter router) {
		super(id, x, y, z);
		for(IRouter iRouter:router.getRouteTable().keySet()) {
			_routeTable.put(iRouter.getId(), router.getRouteTable().get(iRouter));
		}
		for(IRouter iRouter:router._routeCosts.keySet()) {
			_routeCosts.put(iRouter.getId(), router._routeCosts.get(iRouter));
		}
		uuid = router.getId();
		this._dimension = dimension;
		for(int i=0;i<6;i++) {
			routedExit[i] = router.isRoutedExit(Orientations.values()[i]);
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
		for(UUID id:_routeTable.keySet()) {
			data.writeBoolean(true);
			data.writeUTF(id.toString());
			data.writeInt(_routeTable.get(id).ordinal());
		}
		data.writeBoolean(false);
		for(UUID id:_routeCosts.keySet()) {
			data.writeBoolean(true);
			data.writeUTF(id.toString());
			data.writeInt(_routeCosts.get(id));
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
			UUID id = UUID.fromString(data.readUTF());
			Orientations ori = Orientations.values()[data.readInt()];
			_routeTable.put(id, ori);
		}
		while(data.readBoolean()) {
			UUID id = UUID.fromString(data.readUTF());
			int cost = data.readInt();
			_routeCosts.put(id, cost);
		}
		for(int i=0;i<6;i++) {
			routedExit[i] = data.readBoolean();
		}
	}
}
