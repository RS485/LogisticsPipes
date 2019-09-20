package logisticspipes.utils.transactor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.SingleItemSlot;

public class TransactorSimple extends Transactor {

	protected FixedItemInv inventory;

	public TransactorSimple(FixedItemInv inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(ItemStack stack, Direction orientation, boolean doAdd) {
		List<SingleItemSlot> filledSlots = new ArrayList<>(inventory.getSlotCount());
		List<SingleItemSlot> emptySlots = new ArrayList<>(inventory.getSlotCount());
		for (int i = 0; i < inventory.getSlotCount(); i++) {
			SingleItemSlot slot = inventory.getSlot(i);
			if (slot.getInsertionFilter().matches(stack)) {
				if (slot.get().isEmpty()) {
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

	private int tryPut(ItemStack stack, List<SingleItemSlot> slots, int injected, boolean doAdd) {
		int realInjected = injected;

		if (realInjected >= stack.getCount()) {
			return realInjected;
		}

		for (SingleItemSlot slot : slots) {
			ItemStack stackInSlot = slot.get();
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
	protected int addToSlot(SingleItemSlot slot, ItemStack stack, int injected, boolean doAdd) {
		int available = stack.getCount() - injected;

		ItemStack newStack = stack.copy();
		newStack.setCount(available);

		ItemStack rest = slot.attemptInsertion(newStack, doAdd ? Simulation.ACTION : Simulation.SIMULATE);
		return available - rest.getCount();
	}

	private boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
		return !stack1.isEmpty() && !stack2.isEmpty() &&
				stack1.isItemEqual(stack2) &&
				ItemStack.areTagsEqual(stack1, stack2);

	}
}
