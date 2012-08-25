package logisticspipes.logisticspipes;

import net.minecraft.src.IInventory;
import buildcraft.api.core.Orientations;

public interface IInventoryProvider {
	public IInventory getInventory();
	public IInventory getRawInventory();
	public Orientations inventoryOrientation();
}
