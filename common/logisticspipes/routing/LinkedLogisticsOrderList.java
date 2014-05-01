package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class LinkedLogisticsOrderList extends ArrayList<LogisticsOrder> {
	private static final long	serialVersionUID	= 4328359512757178338L;
	
	@Getter
	private List<LinkedLogisticsOrderList> subOrders = new ArrayList<LinkedLogisticsOrderList>();
	
	private List<LogisticsOrder> cachedList = null;
	
	private void generateCache() {
		cachedList = new ArrayList<LogisticsOrder>();
		cachedList.addAll(this);
		for(LinkedLogisticsOrderList sub:subOrders) {
			cachedList.addAll(sub.getList());
		}
	}
	
	public List<LogisticsOrder> getList() {
		if(cachedList == null) generateCache();
		return cachedList;
	}
	
	public int getTreeRootSize() {
		int subSize = 0;
		for(LinkedLogisticsOrderList sub:subOrders) {
			subSize += sub.getTreeRootSize();
		}
		return Math.max(this.size(), subSize);
	}
	
	public int getSubTreeRootSize() {
		int subSize = 0;
		for(LinkedLogisticsOrderList sub:subOrders) {
			subSize += sub.getTreeRootSize();
		}
		return subSize;
	}
}
