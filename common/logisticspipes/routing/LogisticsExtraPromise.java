package logisticspipes.routing;

import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.utils.item.ItemIdentifier;

public class LogisticsExtraPromise extends LogisticsPromise {
	
	public LogisticsExtraPromise(ItemIdentifier item, int numberOfItems, IProvideItems sender, boolean provided) {
		super(item, numberOfItems, sender, null);
		this.provided = provided;
	}

	public boolean provided;
	
	@Override
	public LogisticsExtraPromise copy() {
		return new LogisticsExtraPromise(item, numberOfItems, sender, provided);
	}
}
