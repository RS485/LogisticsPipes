package logisticspipes.utils.gui;

import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.request.resources.DictResource;
import net.minecraft.inventory.IInventory;

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
		return xDisplayPosition;
	}

	@Override
	public int getY() {
		return yDisplayPosition;
	}

	@Override
	public int getSlotId() {
		return this.slotNumber;
	}
}
