package logisticspipes.utils.transactor;

import net.minecraft.util.math.Direction;

import net.minecraftforge.items.IItemHandler;

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
	public static Iterable<IInvSlot> getIterable(IItemHandler inv, Direction side) {

		return new InventoryIteratorSimple(inv);
	}

}
