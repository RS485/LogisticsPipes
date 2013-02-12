package logisticspipes.routing;

public class LogisticsExtraPromise extends LogisticsPromise {
	public boolean provided;
	
	public LogisticsExtraPromise copy() {
		LogisticsExtraPromise result = new LogisticsExtraPromise();
		result.item = item;
		result.numberOfItems = numberOfItems;
		result.sender = sender;
		result.provided = provided;
		return result;
	}
}
