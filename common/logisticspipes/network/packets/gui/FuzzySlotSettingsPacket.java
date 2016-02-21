package logisticspipes.network.packets.gui;

import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.BitSet;

@Accessors(chain = true)
public class FuzzySlotSettingsPacket extends ModernPacket {

	@Getter
	@Setter
	private int slotNumber;

	@Getter
	@Setter
	private BitSet flags;

	public FuzzySlotSettingsPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		slotNumber = data.readInt();
		flags = data.readBitSet();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(player.openContainer != null && player.openContainer.getSlot(slotNumber) instanceof IFuzzySlot) {
			((IFuzzySlot) player.openContainer.getSlot(slotNumber)).getFuzzyFlags().loadFromBitSet(flags);
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(slotNumber);
		data.writeBitSet(flags);
	}

	@Override
	public ModernPacket template() {
		return new FuzzySlotSettingsPacket(getId());
	}
}
