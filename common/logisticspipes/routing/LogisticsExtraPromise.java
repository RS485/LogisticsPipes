package logisticspipes.routing;

import lombok.Getter;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.request.IExtraPromise;
import logisticspipes.request.resources.IResource;
import logisticspipes.utils.item.ItemIdentifier;

public class LogisticsExtraPromise extends LogisticsPromise implements IExtraPromise {

	public LogisticsExtraPromise(ItemIdentifier item, int numberOfItems, IProvideItems sender, boolean provided) {
		super(item, numberOfItems, sender, null);
		this.provided = provided;
	}

	@Getter
	public boolean provided;

	@Override
	public LogisticsExtraPromise copy() {
		return new LogisticsExtraPromise(item, numberOfItems, sender, provided);
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
