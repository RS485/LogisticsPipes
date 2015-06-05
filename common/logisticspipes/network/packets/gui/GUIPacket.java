package logisticspipes.network.packets.gui;

import java.io.IOException;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class GUIPacket extends ModernPacket {

	/**
	 * GUI Type ID
	 */
	@Getter
	@Setter
	private int guiID;

	/**
	 * GUI Count ID
	 */
	@Getter
	@Setter
	private int windowID;

	/**
	 * GUI Additional Information
	 */
	@Getter
	@Setter
	private byte[] guiData;

	public GUIPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		guiID = data.readInt();
		windowID = data.readInt();
		guiData = new byte[data.readInt()];
		data.read(guiData);
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		NewGuiHandler.openGui(this, player);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(guiID);
		data.writeInt(windowID);
		data.writeInt(guiData.length);
		data.write(guiData);
	}

	@Override
	public ModernPacket template() {
		return new GUIPacket(getId());
	}
}
