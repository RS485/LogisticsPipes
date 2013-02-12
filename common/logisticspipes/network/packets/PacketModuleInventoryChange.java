package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.inventory.IInventory;

public class PacketModuleInventoryChange extends PacketInventoryChange {
	public int slot;
	
	public PacketModuleInventoryChange() {
		super();
	}

	public PacketModuleInventoryChange(int id, int x, int y, int z, int slot, IInventory inv) {
		super(id, x, y, z, inv);
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
