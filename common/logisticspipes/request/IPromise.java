package logisticspipes.request;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IProvide;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemIdentifier;

public interface IPromise {

	boolean matches(IResource requestType);

	int getAmount();

	IExtraPromise split(int more);

	IProvide getProvider();

	ItemIdentifier getItemType();

	ResourceType getType();

	IOrderInfoProvider fullFill(IResource requestType, IAdditionalTargetInformation info);

	IPromise copy();
}
