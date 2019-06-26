package logisticspipes.logisticspipes;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.utils.item.ItemIdentifier;

public interface IInventoryProvider extends ISendRoutedItem {

	@Nullable
	public IInventoryUtil getPointedInventory();

	public IInventoryUtil getPointedInventory(ExtractionMode mode, boolean forExtraction);

	public IInventoryUtil getSneakyInventory(boolean forExtraction, ModulePositionType slot, int positionInt);

	public IInventoryUtil getSneakyInventory(EnumFacing _sneakyOrientation);

	public IInventoryUtil getUnsidedInventory();

	public TileEntity getRealInventory();

	public EnumFacing inventoryOrientation();

	// to interact and send items you need to know about orders, upgrades, and have the ability to send
	public LogisticsItemOrderManager getItemOrderManager();

	public void queueRoutedItem(IRoutedItem routedItem, EnumFacing from);

	public ISlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt);

	public int countOnRoute(ItemIdentifier item);
}
