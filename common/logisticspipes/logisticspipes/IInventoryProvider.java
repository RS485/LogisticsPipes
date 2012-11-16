package logisticspipes.logisticspipes;

import net.minecraft.src.IInventory;
import net.minecraftforge.common.ForgeDirection;

public interface IInventoryProvider {
	public IInventory getInventory();
	public IInventory getRawInventory();
	public ForgeDirection inventoryOrientation();
}
