package logisticspipes.interfaces.routing;

import java.util.List;
import java.util.UUID;

import logisticspipes.routing.SearchNode;

public interface IFilteringRouter {
	public List<SearchNode> getRouters();
	public IFilter getFilter();
	public boolean idIdforOtherSide(UUID id);
}
