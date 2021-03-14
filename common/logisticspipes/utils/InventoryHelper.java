package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.transactor.TransactorSimple;
import network.rs485.logisticspipes.inventory.ProviderMode;

public class InventoryHelper {

	//BC getTransactorFor using our getInventory
	public static ITransactor getTransactorFor(Object object, EnumFacing dir) {
		if (object instanceof TileEntity) {
			ITransactor t = SimpleServiceLocator.inventoryUtilFactory.getSpecialHandlerFor((TileEntity) object, dir, ProviderMode.DEFAULT);
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
