package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.network.NetworkConstants;
import logisticspipes.utils.ItemMessage;

public class PacketSimulate extends PacketLogisticsPipes {
	public List<ItemMessage> used = new LinkedList<ItemMessage>();
	public List<ItemMessage> missing = new LinkedList<ItemMessage>();
	private int ID = NetworkConstants.COMPONENT_LIST;
	
	public PacketSimulate() {
		super();
	}
	
	public PacketSimulate(List<ItemMessage> used, List<ItemMessage> missing) {
		this();
		this.used = used;
		this.missing = missing;
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for(ItemMessage msg:used) {
			data.write(1);
			data.writeInt(msg.id);
			data.writeInt(msg.data);
			data.writeInt(msg.amount);
		}
		data.write(0);
		for(ItemMessage msg:missing) {
			data.write(1);
			data.writeInt(msg.id);
			data.writeInt(msg.data);
			data.writeInt(msg.amount);
		}
		data.write(0);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while(data.read() != 0) {
			ItemMessage msg = new ItemMessage();
			msg.id = data.readInt();
			msg.data = data.readInt();
			msg.amount = data.readInt();
			used.add(msg);
		}
		while(data.read() != 0) {
			ItemMessage msg = new ItemMessage();
			msg.id = data.readInt();
			msg.data = data.readInt();
			msg.amount = data.readInt();
			missing.add(msg);
		}
	}

	@Override
	public int getID() {
		return ID;
	}
}
