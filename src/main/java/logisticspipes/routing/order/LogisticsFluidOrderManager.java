package logisticspipes.routing.order;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.FluidIdentifier;

public class LogisticsFluidOrderManager extends LogisticsOrderManager<LogisticsFluidOrder, FluidIdentifier> {

	private static class IC implements LogisticsOrderLinkedList.IIdentityProvider<LogisticsFluidOrder, FluidIdentifier> {

		@Override
		public FluidIdentifier getIdentity(LogisticsFluidOrder o) {
			return o.getFluid();
		}

		@Override
		public boolean isExtra(LogisticsFluidOrder o) {
			return false;
		}
	}

	public LogisticsFluidOrderManager(ILPPositionProvider pos) {
		super(new LogisticsOrderLinkedList<LogisticsFluidOrder, FluidIdentifier>(new IC()), pos);
	}

	public LogisticsFluidOrderManager(IChangeListener listener, ILPPositionProvider pos) {
		super(listener, pos, new LogisticsOrderLinkedList<LogisticsFluidOrder, FluidIdentifier>(new IC()));
	}

	@Override
	public void sendFailed() {
		_orders.getFirst().sendFailed();
		super.sendFailed();
	}

	public LogisticsFluidOrder addOrder(FluidLogisticsPromise promise, IRequestFluid destination, ResourceType type, IAdditionalTargetInformation info) {
		if (promise.amount < 0) {
			throw new RuntimeException("The amount can't be less than zero");
		}
		LogisticsFluidOrder order = new LogisticsFluidOrder(promise.liquid, promise.amount, destination, type, info);
		_orders.addLast(order);
		listen();
		return order;
	}

	public Integer totalFluidsCountInOrders(FluidIdentifier fluid) {
		int itemCount = 0;
		for (LogisticsFluidOrder request : _orders) {
			if (!request.getFluid().equals(fluid)) {
				continue;
			}
			itemCount += request.getAmount();
		}
		return itemCount;
	}
}
