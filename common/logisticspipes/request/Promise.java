package logisticspipes.request;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.RequestProvider;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import network.rs485.logisticspipes.routing.request.Resource;
import network.rs485.logisticspipes.util.ItemVariant;

public interface Promise {

	boolean matches(Resource requestType);

	int getAmount();

	ExtraPromise split(int more);

	RequestProvider getProvider();

	ItemVariant getItemType();

	ResourceType getType();

	IOrderInfoProvider fullFill(Resource requestType, IAdditionalTargetInformation info);

	Promise copy();

}
