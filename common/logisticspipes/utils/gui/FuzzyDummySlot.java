package logisticspipes.utils.gui;

import net.minecraft.inventory.IInventory;

import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.request.resources.DictResource;

public class FuzzyDummySlot extends DummySlot implements IFuzzySlot {

	private DictResource dictResource;

	public FuzzyDummySlot(IInventory iinventory, int i, int j, int k, DictResource dictResource) {
		super(iinventory, i, j, k);
		this.dictResource = dictResource;
	}

	@Override
	public DictResource getFuzzyFlags() {
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
