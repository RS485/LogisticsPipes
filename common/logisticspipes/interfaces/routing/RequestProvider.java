package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.Router;

public interface RequestProvider {

	void tryProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filter);

	Router getRouter();

}
