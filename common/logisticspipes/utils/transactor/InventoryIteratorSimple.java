package logisticspipes.utils.transactor;

import java.util.Iterator;

import javax.annotation.Nonnull;

import logisticspipes.utils.InventoryHelper;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

class InventoryIteratorSimple implements Iterable<IInvSlot> {

	private final IItemHandler inv;

	InventoryIteratorSimple(IItemHandler inv) {
		this.inv = inv;
	}

	@Override
	public Iterator<IInvSlot> iterator() {
		return new Iterator<IInvSlot>() {

			int slot = 0;

			@Override
			public boolean hasNext() {
				return slot < inv.getSlots();
			}

			@Override
			public IInvSlot next() {
				return new InvSlot(slot++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported.");
			}
		};
	}

	private class InvSlot implements IInvSlot {

		private int slot;

		public InvSlot(int slot) {
			this.slot = slot;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot() {
			return inv.getStackInSlot(slot);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate) {
			return inv.insertItem(slot, stack, simulate);
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int amount, boolean simulate) {
			return inv.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit() {
			return inv.getSlotLimit(slot);
		}

		@Override
		public boolean canPutStackInSlot(ItemStack stack) {
			ItemStack toTest = stack.copy();
			toTest.setCount(1);
			return inv.insertItem(slot, toTest, true).isEmpty();
		}
	}
}
