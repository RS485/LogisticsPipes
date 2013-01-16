package logisticspipes.interfaces.routing;

import java.util.List;

import net.minecraftforge.common.ForgeDirection;

import logisticspipes.routing.SearchNode;

public interface IFilteringPipe {
	public List<SearchNode> getRouters(ForgeDirection direction);
	public IFilter getFilter();
}
