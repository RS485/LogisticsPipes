package logisticspipes.interfaces.routing;

import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.Promise;
import logisticspipes.request.resources.Resource;

public interface Crafter extends RequestProvider {

	void registerExtras(Promise promise);

	CraftingTemplate addCrafting(Resource type);

	boolean canCraft(Resource toCraft);

	int getTodo();
}
