package logisticspipes.blocks.stats;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;

import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.StatisticsGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class LogisticsStatisticsTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity {

	public List<TrackingTask> tasks = new ArrayList<>();
	private int tickCount;
	private CoreRoutedPipe cachedConnectedPipe;

	@Override
	public void notifyOfBlockChange() {
		cachedConnectedPipe = null;
	}

	@Override
	public void update() {
		tryUpdateBlockFormat();
		if (MainProxy.isClient(world)) {
			return;
		}
		tickCount++;
		if (getConnectedPipe() == null) {
			return;
		}
		for (TrackingTask task : tasks) {
			task.tick(tickCount, getConnectedPipe());
		}
	}

	@Override
	public void readFromNBT(CompoundTag nbt) {
		super.readFromNBT(nbt);
		int size = nbt.getInteger("taskSize");
		for (int i = 0; i < size; i++) {
			CompoundTag tag = (CompoundTag) nbt.getTag("Task_" + i);
			TrackingTask task = new TrackingTask();
			task.readFromNBT(tag);
			tasks.add(task);
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger("taskSize", tasks.size());
		int count = 0;
		for (TrackingTask task : tasks) {
			CompoundTag tag = new CompoundTag();
			task.writeToNBT(tag);
			nbt.setTag("Task_" + count, tag);
			count++;
		}
		return nbt;
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(StatisticsGui.class).setTrackingList(tasks);
	}

	public CoreRoutedPipe getConnectedPipe() {
		if (cachedConnectedPipe == null) {
			new WorldCoordinatesWrapper(this).allNeighborTileEntities()
					.filter(NeighborBlockEntity::isLogisticsPipe)
					.filter(adjacent -> ((LogisticsTileGenericPipe) adjacent.getBlockEntity()).pipe instanceof CoreRoutedPipe)
					.map(adjacent -> (CoreRoutedPipe) (((LogisticsTileGenericPipe) adjacent.getBlockEntity()).pipe))
					.findFirst()
					.ifPresent(coreRoutedPipe -> cachedConnectedPipe = coreRoutedPipe);
		}
		return cachedConnectedPipe;
	}
}
