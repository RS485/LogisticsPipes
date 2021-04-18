package logisticspipes.interfaces;

import java.util.Collection;
import javax.annotation.Nonnull;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IModuleInventoryReceive {

	void handleInvContent(@Nonnull Collection<ItemIdentifierStack> _allItems);
}
