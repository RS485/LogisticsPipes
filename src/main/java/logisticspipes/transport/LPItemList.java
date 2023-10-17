package logisticspipes.transport;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class LPItemList implements Iterable<LPTravelingItem> {

	private final BiMap<Integer, LPTravelingItem> items = HashBiMap.create();
	private final Set<LPTravelingItem> toLoad = new HashSet<>();
	private final Set<LPTravelingItem> toAdd = new HashSet<>();
	private final Set<LPTravelingItem> toRemove = new HashSet<>();
	private int delay = 0;
	private final PipeTransportLogistics pipe;
	private boolean iterating = false;

	public LPItemList(PipeTransportLogistics pipe) {
		this.pipe = pipe;
	}

	public void add(LPTravelingItem item) {
		if (iterating) {
			toAdd.add(item);
			return;
		}
		if (items.containsValue(item)) {
			return;
		}
		item.setContainer(pipe.container);
		items.put(item.getId(), item);
	}

	private void addAll(Collection<? extends LPTravelingItem> collection) {
		collection.forEach(this::add);
	}

	public LPTravelingItem get(int id) {
		return items.get(id);
	}

	void scheduleLoad(LPTravelingItem item) {
		delay = 10;
		toLoad.add(item);
	}

	private void loadScheduledItems() {
		if (delay > 0) {
			delay--;
			return;
		}
		addAll(toLoad);
		toLoad.clear();
	}

	public void scheduleAdd() {
		iterating = true;

	}

	public void addScheduledItems() {
		iterating = false;
		addAll(toAdd);
		toAdd.clear();
	}

	public boolean scheduleRemoval(LPTravelingItem item) {
		return toRemove.add(item);
	}

	public boolean unscheduleRemoval(LPTravelingItem item) {
		return toRemove.remove(item);
	}

	void removeScheduledItems() {
		items.values().removeAll(toRemove);
		toRemove.clear();
	}

	void purgeBadItems() {
		Iterator<LPTravelingItem> it = items.values().iterator();
		while (it.hasNext()) {
			LPTravelingItem item = it.next();
			if (item.isCorrupted()) {
				it.remove();
				continue;
			}

			if (item.getContainer() != pipe.container) {
				it.remove();
				continue;
			}
		}
	}

	public void flush() {
		loadScheduledItems();
		removeScheduledItems();
		purgeBadItems();
	}

	@Nonnull
	@Override
	public Iterator<LPTravelingItem> iterator() {
		return items.values().iterator();
	}

	void clear() {
		items.clear();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
}
