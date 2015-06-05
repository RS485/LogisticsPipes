package logisticspipes.network.packets.modules;

import java.io.IOException;
import java.util.BitSet;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(pos);
		if (pos != -1) {
			data.writeBoolean(isNBT);
		} else {
			data.writeBitSet(ignoreData);
			data.writeBitSet(ignoreNBT);
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		pos = data.readInt();
		if (pos != -1) {
			isNBT = data.readBoolean();
		} else {
			ignoreData = data.readBitSet();
			ignoreNBT = data.readBitSet();
		}
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkFuzzy(getId());
	}

}
