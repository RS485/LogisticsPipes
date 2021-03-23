package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCQueued;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;

@CCType(name = "FilterInventory")
public class CCFilterInventory {

	private final IItemIdentifierInventory inv;

	public CCFilterInventory(IItemIdentifierInventory inv) {
		this.inv = inv;
	}

	@CCCommand(description = "Returns the size of this FilterInventory")
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@CCCommand(description = "Returns the ItemIdentifier in the given slot")
	@CCQueued
	public ItemIdentifier getItemIdentifier(Double slot) {
		int s = slot.intValue();
		if (s <= 0 || s > getSizeInventory()) {
			throw new UnsupportedOperationException("Slot out of Inventory");
		}
		if (s != slot) {
			throw new UnsupportedOperationException("Slot not an Integer");
		}
		s--;
		if (inv.getIDStackInSlot(s) == null) {
			return null;
		}
		return inv.getIDStackInSlot(s).getItem();
	}

	@CCCommand(description = "Sets the ItemIdentifier at the given slot")
	@CCQueued
	public void setItemIdentifier(Double slot, ItemIdentifier ident) {
		int s = slot.intValue();
		if (s <= 0 || s > getSizeInventory()) {
			throw new UnsupportedOperationException("Slot out of Inventory");
		}
		if (s != slot) {
			throw new UnsupportedOperationException("Slot not an Integer");
		}
		s--;
		inv.setInventorySlotContents(s, ident.makeStack(1));
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
