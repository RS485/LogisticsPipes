package logisticspipes.utils.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class UnmodifiableSlot extends Slot {
	public UnmodifiableSlot(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}
}
