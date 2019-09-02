package logisticspipes.interfaces;

import logisticspipes.request.resources.DictResource;

public interface IFuzzySlot {

	DictResource getFuzzyFlags();

	int getX();

	int getY();

	int getSlotId();
}
