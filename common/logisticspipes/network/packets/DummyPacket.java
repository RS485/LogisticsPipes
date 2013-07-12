package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import logisticspipes.network.abstractpackets.ModernPacket;

public class DummyPacket extends ModernPacket {
	
	public DummyPacket(int id) {
		super(id);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		throw new RuntimeException("This packet should never be used");
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		throw new RuntimeException("This packet should never be used");
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		throw new RuntimeException("This packet should never be used");
	}
	
	@Override
	public ModernPacket template() {
		return new DummyPacket(getId());
	}
}
