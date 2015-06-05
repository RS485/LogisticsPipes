package logisticspipes.network.packets.block;

import java.io.IOException;
import java.util.Iterator;

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
public class RemoveAmoundTask extends CoordinatesPacket {

	@Setter
	@Getter
	private ItemIdentifier item;

	public RemoveAmoundTask(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
		Iterator<TrackingTask> iter = tile.tasks.iterator();
		while (iter.hasNext()) {
			TrackingTask task = iter.next();
			if (task.item == item) {
				iter.remove();
				break;
			}
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
		return new RemoveAmoundTask(getId());
	}
}
