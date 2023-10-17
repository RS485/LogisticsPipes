package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCQueued;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;

@CCType(name = "FilterInventory")
public class CCItemIdentifierInventory {

	private final IItemIdentifierInventory inv;

	public CCItemIdentifierInventory(IItemIdentifierInventory inv) {
		this.inv = inv;
	}

	@CCCommand(description = "Returns the size of this FilterInventory")
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@CCCommand(description = "Returns the ItemIdentifierStack in the given slot")
	@CCQueued
	public ItemIdentifierStack getItemIdentifierStack(Double slot) {
		int s = slot.intValue();
		if (s <= 0 || s > getSizeInventory()) {
			throw new UnsupportedOperationException("Slot out of Inventory");
		}
		if (s != slot) {
			throw new UnsupportedOperationException("Slot not an Integer");
		}
		s--;
		return inv.getIDStackInSlot(s);
	}

	@CCCommand(description = "Sets the ItemIdentifierStack at the given slot")
	@CCQueued
	public void setItemIdentifierStack(Double slot, ItemIdentifierStack stack) {
		int s = slot.intValue();
		if (s <= 0 || s > getSizeInventory()) {
			throw new UnsupportedOperationException("Slot out of Inventory");
		}
		if (s != slot) {
			throw new UnsupportedOperationException("Slot not an Integer");
		}
		s--;
		inv.setInventorySlotContents(s, stack);
	}

	@CCCommand(description = "Sets the ItemIdentifierStack at the given slot")
	@CCQueued
	public void clearSlot(Double slot) {
		int s = slot.intValue();
		if (s <= 0 || s > getSizeInventory()) {
			throw new UnsupportedOperationException("Slot out of Inventory");
		}
		if (s != slot) {
			throw new UnsupportedOperationException("Slot not an Integer");
		}
		s--;
		inv.setInventorySlotContents(s, (ItemIdentifierStack) null);
	}
}
