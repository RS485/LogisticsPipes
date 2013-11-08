package logisticspipes.interfaces;

import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.item.ItemIdentifier;

public interface ILegacyActiveModule {
	void registerPreviousLegacyModules(List<ILegacyActiveModule> previousModules);
	boolean filterAllowsItem(ItemIdentifier item);
	void onBlockRemoval();
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filter);
	public void fullFill(LogisticsPromise promise, IRequestItems destination);
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter);
}
