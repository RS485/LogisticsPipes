package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.order.IDistanceTracker;

public class ItemRoutingInformation {

	private static final Map<UUID, ItemRoutingInformation> storeMap = new HashMap<>();

	public int destinationId = -1;
	public UUID destinationUUID;
	public boolean arrived;
	public int bufferCounter = 0;
	public boolean _doNotBuffer;
	public TransportMode _transportMode = TransportMode.Unknown;
	public List<Integer> jamlist = new ArrayList<>();
	public IDistanceTracker tracker = null;
	public IAdditionalTargetInformation targetInfo;

	private long delay = 640 + MainProxy.getGlobalTick();

	@Getter
	@Setter
	private ItemStack item;

	// the global LP tick in which getTickToTimeOut returns 0.
	public long getTimeOut() {
		return delay;
	}

	// how many ticks until this times out
	public long getTickToTimeOut() {
		return delay - MainProxy.getGlobalTick();
	}

	public void resetDelay() {
		delay = 640 + MainProxy.getGlobalTick();
		if (tracker != null) {
			tracker.setDelay(delay);
		}
	}

	public void setItemTimedout() {
		delay = MainProxy.getGlobalTick() - 1;
		if (tracker != null) {
			tracker.setDelay(delay);
		}
	}

	public void readFromNBT(CompoundTag tag) {
		if (tag.hasUuid("destination_uuid")) {
			destinationUUID = tag.getUuid("destination_uuid");
		}
		arrived = tag.getBoolean("arrived");
		bufferCounter = tag.getInt("buffer_counter");
		_transportMode = TransportMode.values()[tag.getInt("transport_mode")];
		ItemStack stack = ItemStack.fromTag(tag.getCompound("item"));
		setItem(stack);
	}

	public void writeToNBT(CompoundTag nbttagcompound) {
		if (destinationUUID != null) {
			nbttagcompound.putString("destination_uuid", destinationUUID.toString());
		}
		nbttagcompound.putBoolean("arrived", arrived);
		nbttagcompound.putInt("buffer_counter", bufferCounter);
		nbttagcompound.putInt("transport_mode", _transportMode.ordinal());

		CompoundTag nbttagcompound2 = getItem().toTag(new CompoundTag());
		nbttagcompound.put("item", nbttagcompound2);
	}

	public void storeToNBT(CompoundTag tag) {
		UUID uuid = UUID.randomUUID();
		tag.putUuid("store_uuid", uuid);
		this.writeToNBT(tag);
		storeMap.put(uuid, this);
	}

	public static ItemRoutingInformation restoreFromNBT(CompoundTag tag) {
		if (tag.hasUuid("store_uuid")) {
			UUID uuid = tag.getUuid("store_uuid");
			if (storeMap.containsKey(uuid)) {
				ItemRoutingInformation result = storeMap.get(uuid);
				storeMap.remove(uuid);
				return result;
			}
		}
		ItemRoutingInformation info = new ItemRoutingInformation();
		info.readFromNBT(tag);
		return info;
	}

	@Override
	public ItemRoutingInformation clone() {
		ItemRoutingInformation that = new ItemRoutingInformation();
		that.destinationId = destinationId;
		that.destinationUUID = destinationUUID;
		that.arrived = arrived;
		that.bufferCounter = bufferCounter;
		that._doNotBuffer = _doNotBuffer;
		that._transportMode = _transportMode;
		that.jamlist = new ArrayList<>(jamlist);
		that.tracker = tracker;
		that.targetInfo = targetInfo;
		that.item = getItem().copy();
		return that;
	}

	@Override
	public String toString() {
		return String.format("(%s, %d, %s, %s, %s, %d, %s)", item, destinationId, destinationUUID, _transportMode, jamlist, delay, tracker);
	}

	public static class DelayComparator implements Comparator<ItemRoutingInformation> {

		@Override
		public int compare(ItemRoutingInformation o1, ItemRoutingInformation o2) {
			return (int) (o2.getTimeOut() - o1.getTimeOut()); // cast will never overflow because the delta is in 1/20ths of a second.
		}
	}

}
