package logisticspipes.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;

/**
 * A large chest helper implementing hashCode and equals
 * 
 * @author artforz
 */
public class InventoryLargeChestLogistics extends InventoryLargeChest {

	private final IInventory _upperChest;
	private final IInventory _lowerChest;

	public InventoryLargeChestLogistics(String par1Str, IInventory par2IInventory, IInventory par3IInventory) {
		super(par1Str, par2IInventory, par3IInventory);
		_upperChest = par2IInventory;
		_lowerChest = par3IInventory;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InventoryLargeChestLogistics)) {
			return false;
		}
		InventoryLargeChestLogistics b = (InventoryLargeChestLogistics) obj;
		return (_upperChest == b._upperChest && _lowerChest == b._lowerChest);
	}

	@Override
	public int hashCode() {
		return _upperChest.hashCode() ^ _lowerChest.hashCode();
	}
}
