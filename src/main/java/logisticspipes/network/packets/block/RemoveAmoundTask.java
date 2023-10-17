package logisticspipes.network.packets.block;

import java.util.Iterator;

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
public class RemoveAmoundTask extends CoordinatesPacket {

	@Setter
	@Getter
	private ItemIdentifier item;

	public RemoveAmoundTask(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTileAs(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
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
		return new RemoveAmoundTask(getId());
	}
}
