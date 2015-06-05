package logisticspipes.network.packets.debug;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.debug.LogWindow;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void readData(LPDataInputStream data) throws IOException {
		windowID = data.readInt();
		line = data.readUTF();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogWindow.getWindow(windowID).newLine(line);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(windowID);
		data.writeUTF(line);
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
