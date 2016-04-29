package logisticspipes.network.guis.block;

import java.io.IOException;
import java.util.List;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.blocks.stats.TrackingTask;
import logisticspipes.gui.GuiStatistics;
import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class StatisticsGui extends CoordinatesGuiProvider {

	@Getter
	@Setter
	private List<TrackingTask> trackingList;

	public StatisticsGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
		if (tile == null) {
			return null;
		}
		tile.tasks = trackingList;
		GuiStatistics gui = new GuiStatistics(tile);

		DummyContainer dummy = new DummyContainer(player.inventory, null);
		gui.inventorySlots = dummy;

		return gui;
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
		if (tile == null) {
			return null;
		}

		DummyContainer dummy = new DummyContainer(player, null);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new StatisticsGui(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeList(trackingList, (data1, object) -> object.writeToLPData(data1));
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		trackingList = data.readList(data1 -> {
			TrackingTask object = new TrackingTask();
			object.readFromLPData(data1);
			return object;
		});
	}
}
