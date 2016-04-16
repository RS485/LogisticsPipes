package logisticspipes.routing;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.request.IExtraPromise;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import lombok.Getter;

public class LogisticsExtraDictPromise extends LogisticsDictPromise implements IExtraPromise {

	public LogisticsExtraDictPromise(DictResource item, int numberOfItems, IProvideItems sender, boolean provided) {
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
	public void registerExtras(IResource requestType) {
		if (sender instanceof ICraftItems) {
			((ICraftItems) sender).registerExtras(this);
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
