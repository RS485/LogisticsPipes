package logisticspipes.utils.gui;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.ISlotCheck;

public class RestrictedSlot extends Slot {

	private final Item item;
	private final ISlotCheck slotCheck;

	public RestrictedSlot(IInventory iinventory, int i, int j, int k, Class<? extends Item> itemClass) {
		super(iinventory, i, j, k);
		this.item = null;
		slotCheck = itemStack -> !itemStack.isEmpty() && itemClass.isAssignableFrom(itemStack.getItem().getClass());
	}

	public RestrictedSlot(IInventory iinventory, int i, int j, int k, Item item) {
		super(iinventory, i, j, k);
		this.item = item;
		slotCheck = null;
	}

	public RestrictedSlot(IInventory iinventory, int i, int j, int k, ISlotCheck slotCheck) {
		super(iinventory, i, j, k);
		item = null;
		this.slotCheck = slotCheck;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for
	 * the armor slots.
	 */
	@Override
	public boolean isItemValid(@Nonnull ItemStack par1ItemStack) {
		if (slotCheck == null) {
			return par1ItemStack.getItem() == item;
		} else {
			return slotCheck.isStackAllowed(par1ItemStack);
		}
	}
}
