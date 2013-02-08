package logisticspipes.interfaces.routing;

import java.util.List;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.utils.ItemIdentifier;

public interface ICraftMultipleItems extends IRequestItems, IProvideItems {
	void registerExtras(int count);
	void addCraftings(List<CraftingTemplate> lst);
	//void canCraft(LogisticsTransaction transaction);
	void getCraftedItems(List<ItemIdentifier> lst);

}
