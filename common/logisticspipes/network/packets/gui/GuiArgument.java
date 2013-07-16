package logisticspipes.network.packets.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.GuiHandler;
import logisticspipes.network.abstractpackets.GenericPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class GuiArgument extends GenericPacket {

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

