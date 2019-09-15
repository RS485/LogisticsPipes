package logisticspipes.utils;

import net.minecraft.util.math.Direction;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;

import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.transactor.TransactorSimple;

public class InventoryHelper {

	// BC getTransactorFor using our getInventory
	public static ITransactor getTransactorFor(Object object, Direction dir) {
		if (object instanceof ICapabilityProvider) {
			ITransactor t = InventoryUtilFactory.INSTANCE.getUtilForInv((ICapabilityProvider) object, dir, false, false, 0, 0);
			if (t != null) {
				return t;
			}
		}

		if (object instanceof ICapabilityProvider && ((ICapabilityProvider) object).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
			return new TransactorSimple(((ICapabilityProvider) object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir));
		}

		return null;

	}
}
