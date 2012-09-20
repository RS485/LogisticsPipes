package logisticspipes.modules;

import java.util.UUID;

import net.minecraft.src.ItemStack;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.main.CoreRoutedPipe.ItemSendMode;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;

public class ModuleProviderMk2 extends ModuleProvider {
	protected int sendItem(ItemIdentifier item, int maxCount, UUID destination) {
		int sent = 0;
		if (_invProvider.getInventory() == null) return 0;
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		if (inv.itemCount(item)> 0){
			ItemStack removed = inv.getSingleItem(item);
			_itemSender.sendStack(removed, destination, ItemSendMode.Fast);
			sent++;
			maxCount--;
		}
		return sent;
	}
}
