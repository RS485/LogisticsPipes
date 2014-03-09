package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;

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
