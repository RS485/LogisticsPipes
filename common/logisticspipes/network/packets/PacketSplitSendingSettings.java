package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketSplitSendingSettings extends PacketCoordinates {
	public int group;
	public int amountToSplit;
	
	
	public PacketSplitSendingSettings() {
		super();
	}
	
	public PacketSplitSendingSettings(int id, int xCoord, int yCoord, int zCoord, int value, int amount, boolean enabled) {
		super(id, xCoord, yCoord, zCoord);
		group = value;
		amountToSplit = amount;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(group);
		data.writeInt(amountToSplit);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		group = data.readInt();
		amountToSplit = data.readInt();
	}

}
