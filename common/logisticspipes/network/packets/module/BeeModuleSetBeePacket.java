package logisticspipes.network.packets.module;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristSink.FilterType;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.gui.DummyModuleContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class BeeModuleSetBeePacket extends CoordinatesPacket {
	
	@Getter
	@Setter
	private int integer1 = 0;
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
		ModuleApiaristSink sink;
		if(integer1 < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleApiaristSink) {
					sink = (ModuleApiaristSink) dummy.getModule();
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeItemsApiaristSink) {
				sink = (ModuleApiaristSink) ((PipeItemsApiaristSink)pipe.pipe).getLogisticsModule();
			} else if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(integer1 - 1) instanceof ModuleApiaristSink) {
				sink = (ModuleApiaristSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(integer1 - 1);
			} else {
				return;
			}
		}
		if(integer2 >= sink.filter.length) return;
		switch(integer3) {
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
				if(integer4 >= FilterType.values().length) return;
				sink.filter[integer2].filterType = FilterType.values()[integer4];
				break;
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		data.writeInt(integer1);
		data.writeInt(integer2);
		data.writeInt(integer3);
		data.writeInt(integer4);
		if(string1 == null) string1 = "";
		data.writeUTF(string1);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		integer1 = data.readInt();
		integer2 = data.readInt();
		integer3 = data.readInt();
		integer4 = data.readInt();
		string1 = data.readUTF();
	}
}

