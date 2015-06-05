package logisticspipes.interfaces.routing;

import logisticspipes.request.ICraftingTemplate;
import logisticspipes.request.IPromise;
import logisticspipes.request.resources.IResource;

public interface ICraft extends IProvide {

	void registerExtras(IPromise promise);

	ICraftingTemplate addCrafting(IResource type);

	boolean canCraft(IResource toCraft);

	int getTodo();
}
