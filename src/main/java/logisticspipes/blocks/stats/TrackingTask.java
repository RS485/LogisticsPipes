package logisticspipes.blocks.stats;

import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.util.items.ItemStackLoader;

public class TrackingTask {

	public int everyNthTick = 20 * 60;
	public ItemIdentifier item;
	public int arrayPos = 0;
	public long[] amountRecorded = new long[1440]; //24h with 20ticks and 60sec

	public void tick(int tickCount, CoreRoutedPipe pipe) {
		if (tickCount % everyNthTick != 0) {
			return;
		}
		amountRecorded[arrayPos++] = SimpleServiceLocator.logisticsManager.getAmountFor(item, pipe.getRouter().getIRoutersByCost());
		if (arrayPos >= amountRecorded.length) {
			arrayPos = 0;
		}
	}

	public void readFromNBT(NBTTagCompound nbt) {
		int[] amountRecorded_A = nbt.getIntArray("amountRecorded_A");
		int[] amountRecorded_B = nbt.getIntArray("amountRecorded_B");
		for (int i = 0; i < amountRecorded.length; i++) {
			if (i >= amountRecorded_A.length || i >= amountRecorded_B.length) {
				break;
			}
			amountRecorded[i] = (((long) amountRecorded_B[i]) << 32) | amountRecorded_A[i];
		}
		arrayPos = nbt.getInteger("arrayPos");
		item = ItemIdentifier.get(ItemStackLoader.loadAndFixItemStackFromNBT(nbt));
	}

	public void writeToNBT(NBTTagCompound nbt) {
		int[] amountRecorded_A = new int[amountRecorded.length];
		int[] amountRecorded_B = new int[amountRecorded.length];
		for (int i = 0; i < amountRecorded.length; i++) {
			amountRecorded_A[i] = (int) amountRecorded[i];
			amountRecorded_B[i] = (int) (amountRecorded[i] >> 32);
		}
		nbt.setIntArray("amountRecorded_A", amountRecorded_A);
		nbt.setIntArray("amountRecorded_B", amountRecorded_B);
		nbt.setInteger("arrayPos", arrayPos);
		item.makeNormalStack(1).writeToNBT(nbt);
	}

	public void writeToLPData(LPDataOutput output) {
		output.writeLongArray(amountRecorded);
		output.writeInt(arrayPos);
		output.writeItemIdentifier(item);
	}

	public void readFromLPData(LPDataInput input) {
		amountRecorded = input.readLongArray();
		arrayPos = input.readInt();
		item = input.readItemIdentifier();
	}
}
