package logisticspipes.network.packets.modules;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTCoordinatesPacket;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class BeeModule extends NBTCoordinatesPacket {

	@Setter
	@Getter
	private int slot;
	
	public BeeModule(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new BeeModule(getId());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		ModuleApiaristSink sink;
		if(getSlot() == -1 && tile.pipe instanceof PipeItemsApiaristSink) {
			sink = (ModuleApiaristSink) ((PipeItemsApiaristSink)tile.pipe).getLogisticsModule();
		} else if(tile.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)tile.pipe).getLogisticsModule().getSubModule(getSlot()) instanceof ModuleApiaristSink) {
			sink = (ModuleApiaristSink) ((CoreRoutedPipe)tile.pipe).getLogisticsModule().getSubModule(getSlot());
		} else {
			return;
		}
		if(getTag() != null) {
			sink.readFromPacketNBT(getTag());
		}
	}
}

