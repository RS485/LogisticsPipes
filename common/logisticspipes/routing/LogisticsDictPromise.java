package logisticspipes.routing;

import lombok.Getter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ItemRequestProvider;
import logisticspipes.interfaces.routing.ItemRequester;
import logisticspipes.request.ExtraPromise;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.request.resources.Resource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.order.IOrderInfoProvider;

public class LogisticsDictPromise extends LogisticsPromise {

	@Getter
	private Resource.Dict resource;

	public LogisticsDictPromise(Resource.Dict item, int stackSize, ItemRequestProvider sender, IOrderInfoProvider.ResourceType type) {
		super(item.stack.getItem(), stackSize, sender, type);
		this.resource = item;
		this.resource.stack = this.resource.stack.clone();
		this.resource.stack.setStackSize(stackSize);
	}

	@Override
	public ExtraPromise split(int more) {
		numberOfItems -= more;
		this.resource.stack.setStackSize(numberOfItems);
		return new LogisticsExtraDictPromise(getResource().clone(), more, sender, false);
	}

	@Override
	public IOrderInfoProvider fullFill(Resource requestType, IAdditionalTargetInformation info) {
		ItemRequester destination;
		if (requestType instanceof ItemResource) {
			destination = ((ItemResource) requestType).getTarget();
		} else if (requestType instanceof Resource.Dict) {
			destination = ((Resource.Dict) requestType).getTarget();
		} else {
			throw new UnsupportedOperationException();
		}
		return sender.fulfill(this, destination, info);
	}
}
