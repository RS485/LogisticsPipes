package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.blocks.stats.TrackingTask;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class AddItemToTrackPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemIdentifier item;

	public AddItemToTrackPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTileAs(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeItemIdentifier(item);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		item = input.readItemIdentifier();
	}

	@Override
	public ModernPacket template() {
		return new AddItemToTrackPacket(getId());
	}
}
