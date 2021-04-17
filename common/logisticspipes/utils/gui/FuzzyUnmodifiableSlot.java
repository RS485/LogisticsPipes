package logisticspipes.utils.gui;

import net.minecraft.inventory.IInventory;

import logisticspipes.interfaces.IFuzzySlot;
import network.rs485.logisticspipes.property.IBitSet;

public class FuzzyUnmodifiableSlot extends UnmodifiableSlot implements IFuzzySlot {

	private IBitSet fuzzyFlags;

	public FuzzyUnmodifiableSlot(IInventory par1iInventory, int par2, int par3, int par4, IBitSet fuzzyFlags) {
		super(par1iInventory, par2, par3, par4);
		this.fuzzyFlags = fuzzyFlags;
	}

	@Override
	public IBitSet getFuzzyFlags() {
		return fuzzyFlags;
	}

	@Override
	public int getX() {
		return xPos;
	}

	@Override
	public int getY() {
		return yPos;
	}

	@Override
	public int getSlotId() {
		return this.slotNumber;
	}
}
