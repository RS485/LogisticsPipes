package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.NetworkConstants;

public class PacketHUDSettings extends PacketLogisticsPipes {

	public int buttonId;
	public boolean state;
	public int slot;
	
	public PacketHUDSettings() {
		super();
	}
	
	public PacketHUDSettings(int id, boolean state, int slot) {
		super();
		this.buttonId = id;
		this.state = state;
		this.slot = slot;
	}

	@Override
	public int getID() {
		return NetworkConstants.HUD_SETTING_SET;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		buttonId = data.readInt();
		state = data.readBoolean();
		slot = data.readInt();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(buttonId);
		data.writeBoolean(state);
		data.writeInt(slot);
	}
}
