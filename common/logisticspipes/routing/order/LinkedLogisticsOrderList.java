package logisticspipes.routing.order;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class LinkedLogisticsOrderList extends ArrayList<IOrderInfoProvider> {

	private static final long serialVersionUID = 4328359512757178338L;

	@Getter
	private List<LinkedLogisticsOrderList> subOrders = new ArrayList<>();

	private List<IOrderInfoProvider> cachedList = null;
	private List<Float> cachedProgress = null;

	private void generateCache() {
		cachedList = new ArrayList<>();
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
		this.forEach(IOrderInfoProvider::setWatched);
		subOrders.forEach(LinkedLogisticsOrderList::setWatched);
	}

	private void createProgressCache() {
		cachedProgress = new ArrayList<>();
		for (IOrderInfoProvider order : this) {
			order.getProgresses().stream().filter(n -> !cachedProgress.contains(n)).forEach(n -> cachedProgress.add(n));
		}
	}

	public List<Float> getProgresses() {
		if (cachedProgress == null) {
			createProgressCache();
		}
		return cachedProgress;
	}
}
