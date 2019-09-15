package logisticspipes.utils;

import java.util.Collection;
import java.util.HashMap;

import lombok.Getter;

import logisticspipes.utils.tuples.Tuple4;

public class QuickSortChestMarkerStorage {

	@Getter
	private static final QuickSortChestMarkerStorage instance = new QuickSortChestMarkerStorage();

	private QuickSortChestMarkerStorage() {}

	private HashMap<Tuple4<Integer, Integer, Integer, Integer>, Integer> marker = new HashMap<>();

	@Getter
	private boolean isActivated = false;

	public void setSlots(int x, int y, int z, int slot, int pos) {
		if (isActivated) {
			marker.put(new Tuple4<>(x, y, z, slot), pos);
		}
	}

	public void enable() {
		isActivated = true;
	}

	public void disable() {
		marker.clear();
		isActivated = false;
	}

	public Collection<Integer> getMarker() {
		return marker.values();
	}
}
