package logisticspipes.logisticspipes;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.inventory.IInventory;

import net.minecraft.util.EnumFacing;

public interface IInventoryProvider extends ISendRoutedItem {

	public IInventoryUtil getPointedInventory(boolean forExtraction);

	public IInventoryUtil getPointedInventory(ExtractionMode mode, boolean forExtraction);

	public IInventoryUtil getSneakyInventory(boolean forExtraction, ModulePositionType slot, int positionInt);

	public IInventoryUtil getSneakyInventory(EnumFacing _sneakyOrientation, boolean forExtraction);

	public IInventoryUtil getUnsidedInventory();

	public IInventory getRealInventory();

	public EnumFacing inventoryOrientation();

	// to interact and send items you need to know about orders, upgrades, and have the ability to send
	public LogisticsItemOrderManager getItemOrderManager();

	public void queueRoutedItem(IRoutedItem routedItem, EnumFacing from);

	public ISlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt);

	public int countOnRoute(ItemIdentifier item);
}
