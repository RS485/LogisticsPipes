package logisticspipes.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import lombok.Getter;

import logisticspipes.utils.item.SimpleStackInventory;

public class LogicController {

	public SimpleStackInventory diskInv = new SimpleStackInventory(1, "Disk Inv", 1);

	public List<BaseLogicConnection> connections = new ArrayList<>();
	public List<BaseLogicTask> tasks = new ArrayList<>();

	private Thread oldThread = null;
	@Getter
	private boolean unresolvedTasks = false;

	public void calculate(TileEntity tile) {
		if (oldThread != null && oldThread.isAlive()) {
			return;
		}
		for (BaseLogicTask task : tasks) {
			task.syncTick(tile);
		}
		//oldThread = new Thread() { @Override public void run() {
		for (BaseLogicConnection connection : connections) {
			if (!connection.isInvalidConnection()) {
				if (connection.getSource().getAmountOfOutput() <= connection.getSourceIndex()) {
					connection.setInvalidConnection(true);
					continue;
				}
				if (connection.getSource().getOutputParameterType(connection.getSourceIndex()) != connection.getType()) {
					connection.setInvalidConnection(true);
					continue;
				}
				if (connection.getTarget().getAmountOfInput() <= connection.getTargetIndex()) {
					connection.setInvalidConnection(true);
					continue;
				}
				if (connection.getTarget().getInputParameterType(connection.getTargetIndex()) != connection.getType()) {
					connection.setInvalidConnection(true);
					continue;
				}

			}
		}
		List<BaseLogicTask> toDos = new ArrayList<>(tasks);
		while (!toDos.isEmpty()) {
			boolean nothingDone = true;
			Iterator<BaseLogicTask> iter = toDos.iterator();
			while (iter.hasNext()) {
				BaseLogicTask task = iter.next();
				if (task.isCalculated()) {
					iter.remove();
					nothingDone = false;
				}
			}
			for (BaseLogicConnection connection : connections) {
				if (!connection.isInvalidConnection() && connection.getSource().isCalculated()) {
					connection.getTarget().setInputParameter(connection.getTargetIndex(), connection.getSource().getResult(connection.getSourceIndex()));
					nothingDone = false;
				}
			}
			if (nothingDone) {
				unresolvedTasks = true;
				return;
			}
		}
		unresolvedTasks = false;
		/*}};
		oldThread.setDaemon(true);
		oldThread.start();
		//*/
	}

	public void writeToNBT(NBTTagCompound nbt) {
		diskInv.writeToNBT(nbt, "LogicDiskInv");
		//TODO
	}

	public void readFromNBT(NBTTagCompound nbt) {
		diskInv.readFromNBT(nbt, "LogicDiskInv");
		//TODO
	}
}
