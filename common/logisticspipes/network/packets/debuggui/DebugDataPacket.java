package logisticspipes.network.packets.debuggui;

import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

@Accessors(chain = true)
public class DebugDataPacket extends ModernPacket {

	@Getter
	@Setter
	private byte[] payload;

	@Getter
	@Setter
	private int identifier;

	public DebugDataPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		payload = data.readByteArray();
		identifier = data.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		DebugGuiController.instance().handleDataPacket(payload, identifier, player);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeByteArray(payload);
		data.writeInt(identifier);
	}

	@Override
	public ModernPacket template() {
		return new DebugDataPacket(getId());
	}
}
