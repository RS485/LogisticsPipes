package logisticspipes.network.packets.gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	public void readData(LPDataInput input) throws IOException {
		guiID = input.readInt();
		windowID = input.readInt();
		guiData = input.readByteArray();
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		NewGuiHandler.openGui(this, player);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(guiID);
		output.writeInt(windowID);
		output.writeByteArray(guiData);
	}

	@Override
	public ModernPacket template() {
		return new GUIPacket(getId());
	}
}
