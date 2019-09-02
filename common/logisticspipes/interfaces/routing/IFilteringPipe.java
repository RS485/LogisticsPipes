package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;

public interface IFilteringPipe {

	List<ExitRoute> getRouters(IRouter router);

	IFilter getFilter();
}
