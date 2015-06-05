package logisticspipes.network.packets.module;

import java.io.IOException;

import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristSink.FilterType;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class BeeModuleSetBeePacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int integer2 = 0;
	@Getter
	@Setter
	private int integer3 = 0;
	@Getter
	@Setter
	private int integer4 = 0;
	@Getter
	@Setter
	private String string1 = "";

	public BeeModuleSetBeePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new BeeModuleSetBeePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleApiaristSink sink = this.getLogisticsModule(player, ModuleApiaristSink.class);
		if (sink == null) {
			return;
		}
		if (integer2 >= sink.filter.length) {
			return;
		}
		switch (integer3) {
			case 0:
				sink.filter[integer2].firstBee = string1;
				break;
			case 1:
				sink.filter[integer2].secondBee = string1;
				break;
			case 2:
				sink.filter[integer2].filterGroup = integer4;
				break;
			case 3:
				if (integer4 >= FilterType.values().length) {
					return;
				}
				sink.filter[integer2].filterType = FilterType.values()[integer4];
				break;
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(integer2);
		data.writeInt(integer3);
		data.writeInt(integer4);
		if (string1 == null) {
			string1 = "";
		}
		data.writeUTF(string1);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		integer2 = data.readInt();
		integer3 = data.readInt();
		integer4 = data.readInt();
		string1 = data.readUTF();
	}
}
