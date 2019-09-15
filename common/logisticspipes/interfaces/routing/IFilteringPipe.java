package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.Router;

public interface IFilteringPipe {

	List<ExitRoute> getRouters(Router router);

	IFilter getFilter();
}
