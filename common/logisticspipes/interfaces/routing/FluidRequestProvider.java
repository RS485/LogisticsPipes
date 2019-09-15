package logisticspipes.interfaces.routing;

import java.util.Map;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;

import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;

public interface FluidRequestProvider extends RequestProvider {

	Map<FluidKey, Integer> getAvailableFluids();

	IOrderInfoProvider fulfill(FluidLogisticsPromise promise, FluidRequester destination, ResourceType type, IAdditionalTargetInformation info);

}
