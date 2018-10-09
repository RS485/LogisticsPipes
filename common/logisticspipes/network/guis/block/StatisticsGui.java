package logisticspipes.network.guis.block;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.blocks.stats.TrackingTask;
import logisticspipes.gui.GuiStatistics;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
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

		gui.inventorySlots = new DummyContainer(player.inventory, null);

		return gui;
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		LogisticsStatisticsTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsStatisticsTileEntity.class);
		if (tile == null) {
			return null;
		}

		return new DummyContainer(player, null);
	}

	@Override
	public GuiProvider template() {
		return new StatisticsGui(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeCollection(trackingList, (output1, object) -> object.writeToLPData(output1));

	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		trackingList = input.readArrayList(data1 -> {
			TrackingTask object = new TrackingTask();
			object.readFromLPData(data1);
			return object;
		});
	}
}
