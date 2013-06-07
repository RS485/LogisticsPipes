package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.NetworkConstants;

public class PacketBufferTransfer extends PacketLogisticsPipes {
	
	public byte[] content;
	
	public PacketBufferTransfer() {}
	
	public PacketBufferTransfer(byte[] par) {
		content = par;
	}
	
	@Override
	public int getID() {
		return NetworkConstants.BUFFERED_PACKET_TRANSFER;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		content = new byte[data.available()];
		int i=0;
		while(data.available() > 0) {
			content[i++] = data.readByte();
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.write(content);
	}

}
