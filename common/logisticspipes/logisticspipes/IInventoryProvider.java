package logisticspipes.logisticspipes;

import logisticspipes.interfaces.IInventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.ForgeDirection;

public interface IInventoryProvider {
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
}
