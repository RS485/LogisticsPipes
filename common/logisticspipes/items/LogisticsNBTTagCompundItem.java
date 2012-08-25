package logisticspipes.items;

import logisticspipes.main.LogisticsItem;

public class LogisticsNBTTagCompundItem extends LogisticsItem {

	public LogisticsNBTTagCompundItem(int i) {
		super(i);
	}

	@Override
	public boolean getShareTag() {
		return true;
	}
	
}
