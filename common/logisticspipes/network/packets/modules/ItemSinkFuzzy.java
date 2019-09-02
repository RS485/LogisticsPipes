package logisticspipes.network.packets.modules;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ItemSinkFuzzy extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private BitSet ignoreData;

	@Getter
	@Setter
	private BitSet ignoreNBT;

	@Getter
	@Setter
	private int pos = -1;

	@Getter
	@Setter
	private boolean isNBT = false;

	public ItemSinkFuzzy(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player, ModuleItemSink.class);
		if (module == null) {
			return;
		}
		if (pos != -1) {
			if (isNBT) {
				module.setIgnoreNBT(pos, player);
			} else {
				module.setIgnoreData(pos, player);
			}
		} else {
			module.setIgnoreNBT(ignoreNBT);
			module.setIgnoreData(ignoreData);
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(pos);
		if (pos != -1) {
			output.writeBoolean(isNBT);
		} else {
			output.writeBitSet(ignoreData);
			output.writeBitSet(ignoreNBT);
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		pos = input.readInt();
		if (pos != -1) {
			isNBT = input.readBoolean();
		} else {
			ignoreData = input.readBitSet();
			ignoreNBT = input.readBitSet();
		}
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkFuzzy(getId());
	}

}
