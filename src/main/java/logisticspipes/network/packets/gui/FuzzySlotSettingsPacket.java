package logisticspipes.network.packets.gui;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
	public void readData(LPDataInput input) {
		slotNumber = input.readInt();
		flags = input.readBitSet();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (player.openContainer != null && player.openContainer.getSlot(slotNumber) instanceof IFuzzySlot) {
			((IFuzzySlot) player.openContainer.getSlot(slotNumber)).getFuzzyFlags().replaceWith(flags);
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeInt(slotNumber);
		output.writeBitSet(flags);
	}

	@Override
	public ModernPacket template() {
		return new FuzzySlotSettingsPacket(getId());
	}

}
