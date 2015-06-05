package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PipeManagerWatchingPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private boolean start;

	public PipeManagerWatchingPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe.pipe;
		if (start) {
			cPipe.getOrderManager().startWatching(player);
		} else {
			cPipe.getOrderManager().stopWatching(player);
		}
	}

	@Override
	public ModernPacket template() {
		return new PipeManagerWatchingPacket(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(start);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		start = data.readBoolean();
	}
}
