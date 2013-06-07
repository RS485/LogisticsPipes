package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.NetworkConstants;
import logisticspipes.utils.ItemIdentifier;

public class PacketNameUpdatePacket extends PacketLogisticsPipes {
	
	public ItemIdentifier item;
	public String name;
	
	public PacketNameUpdatePacket(ItemIdentifier item) {
		this.item = item;
		this.name = item.getFriendlyName();
	}
	
	public PacketNameUpdatePacket(ItemIdentifier item, String name) {
		this.item = item;
		this.name = name;
	}
	
	public PacketNameUpdatePacket() {}

	@Override
	public int getID() {
		return NetworkConstants.UPDATE_NAMES;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		item = ItemIdentifier.get(data.readInt(), data.readInt(), null);
		name = data.readUTF();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(item.itemID);
		data.writeInt(item.itemDamage);
		data.writeUTF(name);
	}
}
