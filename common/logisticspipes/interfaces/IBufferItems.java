package logisticspipes.interfaces;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface IBufferItems {
	/**
	 * @param stack Item to add to buffer
	 * @param info Additional Target Information
	 * @return amount of items NOT added to the buffer
	 */
	int addToBuffer(ItemIdentifierStack stack, IAdditionalTargetInformation info);
}
