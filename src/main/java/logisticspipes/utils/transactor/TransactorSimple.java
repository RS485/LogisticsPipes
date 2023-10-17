package logisticspipes.utils.transactor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.items.IItemHandler;

public class TransactorSimple extends Transactor {

	protected IItemHandler inventory;

	public TransactorSimple(IItemHandler inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(@Nonnull ItemStack stack, EnumFacing orientation, boolean doAdd) {
		List<IInvSlot> filledSlots = new ArrayList<>(inventory.getSlots());
		List<IInvSlot> emptySlots = new ArrayList<>(inventory.getSlots());
		for (IInvSlot slot : InventoryIterator.getIterable(inventory, orientation)) {
			if (slot.canPutStackInSlot(stack)) {
				if (slot.getStackInSlot().isEmpty()) {
					emptySlots.add(slot);
				} else {
					filledSlots.add(slot);
				}
			}
		}

		int injected = 0;
		injected = tryPut(stack, filledSlots, injected, doAdd);
		injected = tryPut(stack, emptySlots, injected, doAdd);

		return injected;
	}

	private int tryPut(@Nonnull ItemStack stack, List<IInvSlot> slots, int injected, boolean doAdd) {
		int realInjected = injected;

		if (realInjected >= stack.getCount()) {
			return realInjected;
		}

		for (IInvSlot slot : slots) {
			ItemStack stackInSlot = slot.getStackInSlot();
			if (stackInSlot.isEmpty() || canStacksMerge(stackInSlot, stack)) {
				int used = addToSlot(slot, stack, realInjected, doAdd);
				if (used > 0) {
					realInjected += used;
					if (realInjected >= stack.getCount()) {
						return realInjected;
					}
				}
			}
		}

		return realInjected;
	}

	/**
	 * @param slot
	 * @param stack
	 * @param injected Amount not to move?
	 * @param doAdd
	 * @return Return the number of items moved.
	 */
	protected int addToSlot(IInvSlot slot, @Nonnull ItemStack stack, int injected, boolean doAdd) {
		int available = stack.getCount() - injected;

		ItemStack newStack = stack.copy();
		newStack.setCount(available);

		ItemStack rest = slot.insertItem(newStack, !doAdd);
		return available - rest.getCount();
	}

	private boolean canStacksMerge(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
		if (stack1.isEmpty() || stack2.isEmpty()) {
			return false;
		}
		if (!stack1.isItemEqual(stack2)) {
			return false;
		}
		return ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
}
