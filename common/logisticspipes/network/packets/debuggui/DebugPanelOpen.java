package logisticspipes.network.packets.debuggui;

import java.io.IOException;

import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class DebugPanelOpen extends ModernPacket {

	@Setter
	@Getter
	private String name;

	@Getter
	@Setter
	private int identification;

	public DebugPanelOpen(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DebugPanelOpen(getId());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		setName(data.readUTF());
		setIdentification(data.readInt());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeUTF(getName());
		data.writeInt(getIdentification());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		try {
			DebugGuiController.instance().createNewDebugGui(getName(), getIdentification());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
