package logisticspipes.utils;

import java.util.Collection;
import java.util.HashMap;

import lombok.Getter;

import logisticspipes.utils.tuples.Quartet;

public class QuickSortChestMarkerStorage {

	@Getter
	private static final QuickSortChestMarkerStorage instance = new QuickSortChestMarkerStorage();

	private QuickSortChestMarkerStorage() {}

	private HashMap<Quartet<Integer, Integer, Integer, Integer>, Integer> marker = new HashMap<>();

	@Getter
	private boolean isActivated = false;

	public void setSlots(int x, int y, int z, int slot, int pos) {
		if (isActivated) {
			marker.put(new Quartet<>(x, y, z, slot), pos);
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
