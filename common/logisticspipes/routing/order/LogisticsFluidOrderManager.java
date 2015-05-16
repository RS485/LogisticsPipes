package logisticspipes.routing.order;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.FluidIdentifier;

public class LogisticsFluidOrderManager extends LogisticsOrderManager<LogisticsFluidOrder> {

	public LogisticsFluidOrderManager(ILPPositionProvider pos) {
		super(pos);
	}
	
	public LogisticsFluidOrderManager(IChangeListener listener, ILPPositionProvider pos) {
		super(listener, pos);
	}
	
	public void sendFailed() {
		_orders.getFirst().sendFailed();
		super.sendFailed();
	}

	public LogisticsFluidOrder addOrder(FluidLogisticsPromise promise, IRequestFluid destination, ResourceType type, IAdditionalTargetInformation info) {
		if(promise.amount < 0) throw new RuntimeException("The amount can't be less than zero");
		LogisticsFluidOrder order = new LogisticsFluidOrder(promise.liquid, promise.amount, destination, type, info);
		_orders.addLast(order);
		listen();
		return order;
	}

	public Integer totalFluidsCountInOrders(FluidIdentifier fluid) {
		int itemCount = 0;
		for(LogisticsFluidOrder request: _orders) {
			if(!request.getFluid().equals(fluid)) continue;
			itemCount += request.getAmount();
		}
		return itemCount;
	}
}
