package logisticspipes.interfaces.routing;

import java.util.List;

import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.IRouter;

public interface IProvide {

	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filter);

	public IRouter getRouter();

}
