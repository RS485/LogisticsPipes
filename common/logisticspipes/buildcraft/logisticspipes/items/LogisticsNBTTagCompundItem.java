package logisticspipes.buildcraft.logisticspipes.items;

import logisticspipes.buildcraft.krapht.LogisticsItem;

public class LogisticsNBTTagCompundItem extends LogisticsItem {

	public LogisticsNBTTagCompundItem(int i) {
		super(i);
	}

	@Override
	public boolean getShareTag() {
		return true;
	}
	
}
