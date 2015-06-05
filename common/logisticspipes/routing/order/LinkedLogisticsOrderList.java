package logisticspipes.routing.order;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class LinkedLogisticsOrderList extends ArrayList<IOrderInfoProvider> {

	private static final long serialVersionUID = 4328359512757178338L;

	@Getter
	private List<LinkedLogisticsOrderList> subOrders = new ArrayList<LinkedLogisticsOrderList>();

	private List<IOrderInfoProvider> cachedList = null;
	private List<Float> cachedProgress = null;

	private void generateCache() {
		cachedList = new ArrayList<IOrderInfoProvider>();
		cachedList.addAll(this);
		for (LinkedLogisticsOrderList sub : subOrders) {
			cachedList.addAll(sub.getList());
		}
	}

	public List<IOrderInfoProvider> getList() {
		if (cachedList == null) {
			generateCache();
		}
		return cachedList;
	}

	public int getTreeRootSize() {
		int subSize = 0;
		for (LinkedLogisticsOrderList sub : subOrders) {
			subSize += sub.getTreeRootSize();
		}
		return Math.max(size(), subSize);
	}

	public int getSubTreeRootSize() {
		int subSize = 0;
		for (LinkedLogisticsOrderList sub : subOrders) {
			subSize += sub.getTreeRootSize();
		}
		return subSize;
	}

	public void setWatched() {
		for (IOrderInfoProvider order : this) {
			order.setWatched();
		}
		for (LinkedLogisticsOrderList sub : subOrders) {
			sub.setWatched();
		}
	}

	private void createProgressCache() {
		cachedProgress = new ArrayList<Float>();
		for (IOrderInfoProvider order : this) {
			for (Float n : order.getProgresses()) {
				if (!cachedProgress.contains(n)) {
					cachedProgress.add(n);
				}
			}
		}
	}

	public List<Float> getProgresses() {
		if (cachedProgress == null) {
			createProgressCache();
		}
		return cachedProgress;
	}
}
