package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristSink.FilterType;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(integer2);
		output.writeInt(integer3);
		output.writeInt(integer4);
		if (string1 == null) {
			string1 = "";
		}
		output.writeUTF(string1);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		integer2 = input.readInt();
		integer3 = input.readInt();
		integer4 = input.readInt();
		string1 = input.readUTF();
	}
}
