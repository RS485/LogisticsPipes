package logisticspipes.utils.gui;

import logisticspipes.interfaces.ISlotClick;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class HandelableSlot extends Slot {
	
	private final ISlotClick _handler;
	
	public HandelableSlot(IInventory inventory, int slotId, int xCoord, int yCoord, ISlotClick handler) {
		super(inventory, slotId, xCoord, yCoord);
		_handler = handler;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {	
    	return par1ItemStack == null;
    }

	public ItemStack getProvidedStack() {
		return _handler.getResultForClick();
	}
}
