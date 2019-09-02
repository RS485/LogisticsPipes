package logisticspipes.utils;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.transactor.TransactorSimple;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;

public class InventoryHelper {
	//BC getTransactorFor using our getInventory
	public static ITransactor getTransactorFor(Object object, EnumFacing dir) {
		if (object instanceof ICapabilityProvider) {
			ITransactor t = SimpleServiceLocator.inventoryUtilFactory.getUtilForInv((ICapabilityProvider) object, dir, false, false, 0, 0);
			if (t != null) {
				return t;
			}
		}

		if (object instanceof ICapabilityProvider && ((ICapabilityProvider)object).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
			return new TransactorSimple(((ICapabilityProvider)object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir));
		}

		return null;

	}
}
