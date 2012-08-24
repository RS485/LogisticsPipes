package logisticspipes.krapht.gui;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class RestrictedSlot extends Slot {
	
	private final int ItemID;
	
	public RestrictedSlot(IInventory iinventory, int i, int j, int k, int ItemID) {
		super(iinventory, i, j, k);
		this.ItemID = ItemID;
	}
	
    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack par1ItemStack)
    {
        return par1ItemStack.itemID == ItemID;
    }

}
