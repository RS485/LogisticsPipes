package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.routing.IRouter;
import logisticspipes.routing.ExitRoute;

public interface IFilteringPipe {
	public List<ExitRoute> getRouters(IRouter router);
	public IFilter getFilter();
}
