package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.routing.ExitRoute;

public interface IFilteringRouter {
	public List<ExitRoute> getRouters();
	public IFilter getFilter();
	public boolean idIdforOtherSide(int destination);
	public int getSimpleID();
}
