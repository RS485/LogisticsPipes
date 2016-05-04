package logisticspipes.network.packets.debug;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.debug.LogWindow;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class SendNewLogLine extends ModernPacket {

	@Getter
	@Setter
	private int windowID;

	@Getter
	@Setter
	private String line;

	public SendNewLogLine(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		windowID = input.readInt();
		line = input.readUTF();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogWindow.getWindow(windowID).newLine(line);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(windowID);
		output.writeUTF(line);
	}

	@Override
	public ModernPacket template() {
		return new SendNewLogLine(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
