package logisticspipes.network.packets.debuggui;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.commands.commands.debug.DebugGuiController;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
	public void readData(LPDataInput input) {
		payload = input.readByteArray();
		identifier = input.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		DebugGuiController.instance().handleDataPacket(payload, identifier, player);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeByteArray(payload);
		output.writeInt(identifier);
	}

	@Override
	public ModernPacket template() {
		return new DebugDataPacket(getId());
	}
}
