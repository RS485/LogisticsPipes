package logisticspipes.routing;

import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.utils.ItemIdentifier;

public class LogisticsExtraPromise extends LogisticsPromise {
	public RequestTreeNode extraSource;
	
	public LogisticsExtraPromise copy() {
		LogisticsExtraPromise result = new LogisticsExtraPromise();
		result.item = item;
		result.numberOfItems = numberOfItems;
		result.sender = sender;
		result.extraSource = extraSource;
		return result;
	}
}
