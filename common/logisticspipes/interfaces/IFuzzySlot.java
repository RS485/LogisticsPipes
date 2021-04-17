package logisticspipes.interfaces;

import network.rs485.logisticspipes.property.IBitSet;

public interface IFuzzySlot {

	IBitSet getFuzzyFlags();

	int getX();

	int getY();

	int getSlotId();
}
