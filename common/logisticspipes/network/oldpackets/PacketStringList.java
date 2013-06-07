package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PacketStringList extends PacketLogisticsPipes {
	
	private final int id;
	public List<String> list;

	public PacketStringList() {
		id = 0;
		list = new LinkedList<String>();
	}
	
	public PacketStringList(int id, List<String> list) {
		this.id = id;
		this.list = list;
	}
	
	@Override
	public int getID() {
		return id;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		int size = data.readInt();
		for(int i=0;i<size;i++) {
			list.add(data.readUTF());
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(list.size());
		for(int i=0;i<list.size();i++) {
			data.writeUTF(list.get(i));
		}
	}
}
