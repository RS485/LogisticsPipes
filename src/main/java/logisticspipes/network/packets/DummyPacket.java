package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class DummyPacket extends ModernPacket {

	public DummyPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		throw new RuntimeException("This packet should never be used");
	}

	@Override
	public void processPacket(EntityPlayer player) {
		throw new RuntimeException("This packet should never be used");
	}

	@Override
	public void writeData(LPDataOutput output) {
		throw new RuntimeException("This packet should never be used");
	}

	@Override
	public ModernPacket template() {
		return new DummyPacket(getId());
	}
}
