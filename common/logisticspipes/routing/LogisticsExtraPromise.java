package logisticspipes.routing;

import net.minecraft.item.ItemStack;

import lombok.Getter;

import logisticspipes.interfaces.routing.ItemCrafter;
import logisticspipes.interfaces.routing.ItemRequestProvider;
import logisticspipes.request.ExtraPromise;
import network.rs485.logisticspipes.routing.request.Resource;

public class LogisticsExtraPromise extends LogisticsPromise implements ExtraPromise {

	public LogisticsExtraPromise(ItemStack stack, ItemRequestProvider sender, boolean provided) {
		super(stack, sender, null);
		this.provided = provided;
	}

	@Getter
	public boolean provided;

	@Override
	public LogisticsExtraPromise copy() {
		return new LogisticsExtraPromise(stack, sender, provided);
	}

	@Override
	public void registerExtras(Resource requestType) {
		if (sender instanceof ItemCrafter) {
			((ItemCrafter) sender).registerExtras(this);
		}
	}

	@Override
	public void lowerAmount(int usedCount) {
		stack.decrement(usedCount);
	}

	@Override
	public void setAmount(int amount) {
		stack.setCount(amount);
	}
}
