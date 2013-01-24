package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.routing.SearchNode;

public interface IFilteringRouter {
	public List<SearchNode> getRouters();
	public IFilter getFilter();
	public boolean idIdforOtherSide(int destination);
}
