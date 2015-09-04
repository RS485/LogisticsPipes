package logisticspipes.blocks.stats;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TrackingTask {

	public int everyNthTick = 20 * 60;
	public ItemIdentifier item;
	public int arrayPos = 0;
	public long[] amountRecorded = new long[1440]; //24h with 20ticks and 60sec

	public void tick(int tickCount, CoreRoutedPipe pipe) {
		if (tickCount % everyNthTick != 0) {
			return;
		}
		IRouter router = pipe.getRouter();
		if (router == null) {
			return;
		}
		amountRecorded[arrayPos++] = SimpleServiceLocator.logisticsManager.getAmountFor(item, router.getIRoutersByCost());
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
		item = ItemIdentifier.get(ItemStack.loadItemStackFromNBT(nbt));
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

	public void writeToLPData(LPDataOutputStream data) throws IOException {
		data.writeLongArray(amountRecorded);
		data.writeInt(arrayPos);
		data.writeItemIdentifier(item);
	}

	public void readFromLPData(LPDataInputStream data) throws IOException {
		amountRecorded = data.readLongArray();
		arrayPos = data.readInt();
		item = data.readItemIdentifier();
	}
}
