package logisticspipes.routing;

import lombok.Getter;

import logisticspipes.interfaces.routing.ItemCrafter;
import logisticspipes.interfaces.routing.ItemRequestProvider;
import logisticspipes.request.ExtraPromise;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.request.resources.Resource;

public class LogisticsExtraDictPromise extends LogisticsDictPromise implements ExtraPromise {

	public LogisticsExtraDictPromise(Resource.Dict item, int numberOfItems, ItemRequestProvider sender, boolean provided) {
		super(item, numberOfItems, sender, null);
		this.provided = provided;
	}

	@Getter
	public boolean provided;

	@Override
	public LogisticsExtraDictPromise copy() {
		return new LogisticsExtraDictPromise(getResource(), numberOfItems, sender, provided);
	}

	@Override
	public void registerExtras(Resource requestType) {
		if (sender instanceof ItemCrafter) {
			((ItemCrafter) sender).registerExtras(this);
		}
	}

	@Override
	public void lowerAmount(int usedcount) {
		numberOfItems -= usedcount;
	}

	@Override
	public void setAmount(int amount) {
		numberOfItems = amount;
	}
}
