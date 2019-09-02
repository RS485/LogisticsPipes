package logisticspipes.routing;

import lombok.Getter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.IExtraPromise;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.order.IOrderInfoProvider;

public class LogisticsDictPromise extends LogisticsPromise {

	@Getter
	private DictResource resource;

	public LogisticsDictPromise(DictResource item, int stackSize, IProvideItems sender, IOrderInfoProvider.ResourceType type) {
		super(item.stack.getItem(), stackSize, sender, type);
		this.resource = item;
		this.resource.stack = this.resource.stack.clone();
		this.resource.stack.setStackSize(stackSize);
	}

	@Override
	public IExtraPromise split(int more) {
		numberOfItems -= more;
		this.resource.stack.setStackSize(numberOfItems);
		return new LogisticsExtraDictPromise(getResource().clone(), more, sender, false);
	}

	@Override
	public IOrderInfoProvider fullFill(IResource requestType, IAdditionalTargetInformation info) {
		IRequestItems destination;
		if (requestType instanceof ItemResource) {
			destination = ((ItemResource) requestType).getTarget();
		} else if (requestType instanceof DictResource) {
			destination = ((DictResource) requestType).getTarget();
		} else {
			throw new UnsupportedOperationException();
		}
		return sender.fullFill(this, destination, info);
	}
}
