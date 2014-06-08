package logisticspipes.logisticspipes;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public interface IInventoryProvider extends ISendRoutedItem {
	public IInventoryUtil getPointedInventory(boolean forExtraction);
	public IInventoryUtil getPointedInventory(ExtractionMode mode, boolean forExtraction);
	public IInventoryUtil getSneakyInventory(boolean forExtraction);
	public IInventoryUtil getSneakyInventory(ForgeDirection _sneakyOrientation, boolean forExtraction);
	public IInventoryUtil getUnsidedInventory();
	public IInventory getRealInventory();
	//public IInventory getPointedInventory();
	//public IInventory getSneakyInventory();
	//public IInventory getSneakyInventory(ForgeDirection _sneakyOrientation);
	//public IInventory getRawInventory();
	public ForgeDirection inventoryOrientation();
	public int getX(); // returns the coords of the pipe, so modules which care don't need to fetch the coords for actions they send 
	public int getY();
	public int getZ();
	public World getWorld(); // not much point getting x,y,z but not dimension
	
	// to interact and send items you need to know about orders, upgrades, and have the ability to send
	public LogisticsOrderManager getOrderManager();
	public void queueRoutedItem(IRoutedItem routedItem, ForgeDirection from);
	public UpgradeManager getUpgradeManager();
	public int countOnRoute(ItemIdentifier item); 
}
