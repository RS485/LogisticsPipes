package logisticspipes.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class BufferTransfer extends ModernPacket {

	@Getter
	@Setter
	private byte[] content;

	public BufferTransfer(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new BufferTransfer(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			SimpleServiceLocator.clientBufferHandler.handlePacket(content);
		} else {
			SimpleServiceLocator.serverBufferHandler.handlePacket(content, player);
		}
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		content = input.readLengthAndBytes();
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeLengthAndBytes(content);
	}
}
