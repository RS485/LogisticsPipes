package logisticspipes.transport;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class LPItemList implements Iterable<LPTravelingItem> {

	private final BiMap<Integer, LPTravelingItem> items = HashBiMap.create();
	private final Set<LPTravelingItem> toLoad = new HashSet<LPTravelingItem>();
	private final Set<LPTravelingItem> toAdd = new HashSet<LPTravelingItem>();
	private final Set<LPTravelingItem> toRemove = new HashSet<LPTravelingItem>();
	private int delay = 0;
	private final PipeTransportLogistics pipe;

	public LPItemList(PipeTransportLogistics pipe) {
		this.pipe = pipe;
	}

	private boolean add(LPTravelingItem item) {
		if (items.containsValue(item))
			return false;
		item.setContainer(pipe.container);
		items.put(item.getId(), item);
		return true;
	}

	private boolean addAll(Collection<? extends LPTravelingItem> collection) {
		boolean changed = false;
		for (LPTravelingItem item : collection) {
			changed |= add(item);
		}
		return changed;
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

	public void scheduleAdd(LPTravelingItem item) {
		toAdd.add(item);
	}

	private void addScheduledItems() {
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

	void flush() {
		loadScheduledItems();
		addScheduledItems();
		removeScheduledItems();
		purgeBadItems();
	}

	@Override
	public Iterator<LPTravelingItem> iterator() {
		return items.values().iterator();
	}

	public int size() {
		return items.values().size();
	}

	void clear() {
		toRemove.addAll(items.values());
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	public boolean hasContent() {
		return !items.isEmpty() || !toAdd.isEmpty();
	}
}
