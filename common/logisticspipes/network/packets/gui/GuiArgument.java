package logisticspipes.network.packets.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.GuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.UniversalPacket;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;

public class GuiArgument extends UniversalPacket {

	@Getter
	@Setter
	private int guiID;
	
	public GuiArgument(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new GuiArgument(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		GuiHandler.argumentQueue.put(getGuiID(), getArgs());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		setGuiID(data.readInt());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getGuiID());
	}
}

