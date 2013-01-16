package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;

public interface IFilteringPipe {
	public List<SearchNode> getRouters(IRouter router);
	public IFilter getFilter();
}
