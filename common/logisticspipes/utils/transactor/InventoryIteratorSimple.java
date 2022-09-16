package logisticspipes.utils.transactor;

import java.util.Iterator;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

class InventoryIteratorSimple implements Iterable<IInvSlot> {

	private final IItemHandler inv;

	InventoryIteratorSimple(IItemHandler inv) {
		this.inv = inv;
	}

	@Nonnull
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

		};
	}

	private class InvSlot implements IInvSlot {

		private final int slot;

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
			return inv.insertItem(slot, stack.copy(), simulate);
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
		public boolean canPutStackInSlot(@Nonnull ItemStack stack) {
			ItemStack toTest = stack.copy();
			toTest.setCount(1);
			return inv.insertItem(slot, toTest, true).isEmpty();
		}
	}
}
