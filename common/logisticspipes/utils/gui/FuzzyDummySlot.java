package logisticspipes.utils.gui;

import net.minecraft.inventory.IInventory;

import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.request.resources.Resource.Dict;

public class FuzzyDummySlot extends DummySlot implements IFuzzySlot {

	private Resource.Dict dictResource;

	public FuzzyDummySlot(IInventory iinventory, int i, int j, int k, Resource.Dict dictResource) {
		super(iinventory, i, j, k);
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
