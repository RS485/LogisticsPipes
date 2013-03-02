package logisticspipes.logisticspipes;

import logisticspipes.utils.SneakyOrientation;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.ForgeDirection;

public interface IInventoryProvider {
	public IInventory getPointedInventory();
	public IInventory getSneakyInventory();
	public IInventory getSneakyInventory(ForgeDirection _sneakyOrientation);
	public IInventory getRawInventory();
	public ForgeDirection inventoryOrientation();
}
