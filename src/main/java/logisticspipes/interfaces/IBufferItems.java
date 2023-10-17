package logisticspipes.interfaces;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface IBufferItems {

	/**
	 * @param stack Item to add to buffer. Class will store a copy or other
	 *              representation of stack.
	 * @param info  Additional Target Information
	 * @return amount of items NOT added to the buffer
	 */
	int addToBuffer(ItemIdentifierStack stack, IAdditionalTargetInformation info);
}
