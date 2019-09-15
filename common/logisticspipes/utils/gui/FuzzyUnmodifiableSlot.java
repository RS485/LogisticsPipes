package logisticspipes.utils.gui;

import net.minecraft.inventory.IInventory;

import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.request.resources.Resource.Dict;

public class FuzzyUnmodifiableSlot extends UnmodifiableSlot implements IFuzzySlot {

	private Resource.Dict dictResource;

	public FuzzyUnmodifiableSlot(IInventory par1iInventory, int par2, int par3, int par4, Resource.Dict dictResource) {
		super(par1iInventory, par2, par3, par4);
		this.dictResource = dictResource;
	}

	@Override
	public Resource.Dict getFuzzyFlags() {
		return dictResource;
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
