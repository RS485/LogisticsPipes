package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public class PacketModuleInvContent extends PacketPipeInvContent {
	
	public int slot;
	
	public PacketModuleInvContent() {
		super();
	}

	public PacketModuleInvContent(int id, int x, int y, int z, int slot, List<ItemIdentifierStack> allItems) {
		super(id, x, y, z, allItems);
		this.slot = slot;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
	}
}
