package logisticspipes.blocks.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.StatisticsGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.world.WorldCoordinatesWrapper;

import net.minecraft.nbt.NBTTagCompound;

public class LogisticsStatisticsTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity {

	public List<TrackingTask> tasks = new ArrayList<TrackingTask>();
	private int tickCount;
	private CoreRoutedPipe cachedConnectedPipe;

	@Override
	public void notifyOfBlockChange() {
		cachedConnectedPipe = null;
	}

	@Override
	public void updateEntity() {
		if (MainProxy.isClient(worldObj)) {
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
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		int size = nbt.getInteger("taskSize");
		for (int i = 0; i < size; i++) {
			NBTTagCompound tag = (NBTTagCompound) nbt.getTag("Task_" + i);
			TrackingTask task = new TrackingTask();
			task.readFromNBT(tag);
			tasks.add(task);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("taskSize", tasks.size());
		int count = 0;
		for (TrackingTask task : tasks) {
			NBTTagCompound tag = new NBTTagCompound();
			task.writeToNBT(tag);
			nbt.setTag("Task_" + count, tag);
			count++;
		}
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(StatisticsGui.class).setTrackingList(tasks);
	}

	public CoreRoutedPipe getConnectedPipe() {
		if (cachedConnectedPipe == null) {
			Optional<CoreRoutedPipe> first = new WorldCoordinatesWrapper(this).getAdjacentTileEntities()
					.filter(adjacent -> adjacent.tileEntity instanceof LogisticsTileGenericPipe)
					.filter(adjacent -> ((LogisticsTileGenericPipe) adjacent.tileEntity).pipe instanceof CoreRoutedPipe)
					.map(adjacent -> (CoreRoutedPipe) (((LogisticsTileGenericPipe) adjacent.tileEntity).pipe)).findFirst();

			if (first.isPresent()) {
				cachedConnectedPipe = first.get();
			}
		}
		return cachedConnectedPipe;
	}
}
