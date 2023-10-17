package logisticspipes.routing.order;

import javax.annotation.Nonnull;

import lombok.Getter;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class LogisticsFluidOrder extends LogisticsOrder {

	public LogisticsFluidOrder(FluidIdentifier fuild, Integer amount, IRequestFluid destination, ResourceType type, IAdditionalTargetInformation info) {
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
	private final IRequestFluid destination;

	@Override
	public ItemIdentifierStack getAsDisplayItem() {
		return fluid.getItemIdentifier().makeStack(amount);
	}

	@Override
	@Nonnull
	public IRouter getRouter() {
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
