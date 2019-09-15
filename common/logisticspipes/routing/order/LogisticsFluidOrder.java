package logisticspipes.routing.order;

import lombok.Getter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.FluidRequester;
import logisticspipes.routing.Router;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemStack;

public class LogisticsFluidOrder extends LogisticsOrder {

	public LogisticsFluidOrder(FluidIdentifier fuild, Integer amount, FluidRequester destination, ResourceType type, IAdditionalTargetInformation info) {
		super(type, info);
		if (destination == null) {
			throw new NullPointerException();
		}
		fluid = fuild;
		this.amount = amount;
		this.destination = destination;
	}

	@Getter
	private final FluidIdentifier fluid;
	@Getter
	private int amount;
	private final FluidRequester destination;

	@Override
	public ItemStack getAsDisplayItem() {
		return fluid.getItemIdentifier().makeStack(amount);
	}

	@Override
	public Router getRouter() {
		return destination.getRouter();
	}

	@Override
	public void sendFailed() {
		destination.sendFailed(fluid, amount);
	}

	@Override
	public void reduceAmountBy(int reduce) {
		amount -= reduce;
	}
}
