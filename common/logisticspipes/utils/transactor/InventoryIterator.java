package logisticspipes.utils.transactor;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;

import net.minecraft.util.EnumFacing;

public final class InventoryIterator {

	/**
	 * Deactivate constructor
	 */
	private InventoryIterator() {}

	/**
	 * Returns an Iterable object for the specified side of the inventory.
	 *
	 * @param inv
	 * @param side
	 * @return Iterable
	 */
	public static Iterable<IInvSlot> getIterable(IInventory inv, EnumFacing side) {
		if (inv instanceof ISidedInventory) {
			return new InventoryIteratorSided((ISidedInventory) inv, side);
		}

		return new InventoryIteratorSimple(inv);
	}

}
