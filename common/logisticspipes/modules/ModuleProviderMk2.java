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

	@Override
	public void tick() {
		if (++currentTick < ticksToAction) return;
		currentTick = 0;
		for(int i=0;i<16;i++) {
			if (!_orderManager.hasOrders()) return;
			Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
			int sent = sendItem(order.getValue1().getItem(), order.getValue1().stackSize, order.getValue2().getRouter().getId());
			if (sent > 0){
				_orderManager.sendSuccessfull(sent);
			} else {
				_orderManager.sendFailed();
			}
		}
	}
	
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
