package logisticspipes.network.packets.block;

import java.io.IOException;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.blocks.stats.TrackingTask;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class AddItemToTrackPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemIdentifier item;

	public AddItemToTrackPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
		boolean found = false;
		for (TrackingTask task : tile.tasks) {
			if (task.item.equals(item)) {
				found = true;
				break;
			}
		}
		if (!found) {
			TrackingTask task = new TrackingTask();
			task.item = item;
			tile.tasks.add(task);
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeItemIdentifier(item);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		item = data.readItemIdentifier();
	}

	@Override
	public ModernPacket template() {
		return new AddItemToTrackPacket(getId());
	}
}
