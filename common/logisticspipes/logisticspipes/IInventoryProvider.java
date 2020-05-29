package logisticspipes.logisticspipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.NeighborTileEntity;

public interface IInventoryProvider extends ISendRoutedItem {

	@Nullable
	IInventoryUtil getPointedInventory();

	@Nullable
	IInventoryUtil getPointedInventory(ExtractionMode mode);

	@Nullable
	IInventoryUtil getSneakyInventory(ModulePositionType slot, int positionInt);

	@Nullable
	IInventoryUtil getSneakyInventory(@Nonnull EnumFacing direction);

	@Nullable
	NeighborTileEntity<TileEntity> getPointedItemHandler();

	@Nullable
	EnumFacing getPointedOrientation();

	// to interact and send items you need to know about orders, upgrades, and have the ability to send
	LogisticsItemOrderManager getItemOrderManager();

	void queueRoutedItem(IRoutedItem routedItem, EnumFacing from);

	ISlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt);

	int countOnRoute(ItemIdentifier item);
}
