package logisticspipes.network.packets.debuggui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.network.abstractpackets.ModernPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	public void readData(LPDataInput input) throws IOException {
		payload = input.readLengthAndBytes();
		identifier = input.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		DebugGuiController.instance().handleDataPacket(payload, identifier, player);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeByteArray(payload);
		output.writeInt(identifier);
	}

	@Override
	public ModernPacket template() {
		return new DebugDataPacket(getId());
	}
}
