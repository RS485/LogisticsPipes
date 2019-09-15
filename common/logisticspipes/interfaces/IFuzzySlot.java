package logisticspipes.interfaces;

import logisticspipes.request.resources.Resource.Dict;

public interface IFuzzySlot {

	Resource.Dict getFuzzyFlags();

	int getX();

	int getY();

	int getSlotId();
}
