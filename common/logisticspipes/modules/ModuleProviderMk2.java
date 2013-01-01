package logisticspipes.modules;

import java.util.UUID;

import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;

public class ModuleProviderMk2 extends ModuleProvider {
	protected int sendItem(ItemIdentifier item, int maxCount, UUID destination) {
		int sent = 0;
		if (_invProvider.getInventory() == null) return 0;
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		if (inv.itemCount(item)> 0){
			ItemStack removed = inv.getSingleItem(item);
			if(removed != null) {
				_itemSender.sendStack(removed, destination, ItemSendMode.Fast);
				sent++;
				maxCount--;
			}
		}
		return sent;
	}

	@Override
	protected int neededEnergy() {
		return 2;
	}
}
