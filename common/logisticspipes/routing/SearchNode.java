package logisticspipes.routing;

import java.util.EnumSet;

public class SearchNode implements Comparable<SearchNode>{
	public SearchNode(IRouter r,int d, EnumSet<PipeRoutingConnectionType> enumSet,IRouter p){
		distance=d;
		connectionFlags=enumSet;
		node=r;
		parent=p;
	}
	public int distance;
	public EnumSet<PipeRoutingConnectionType> connectionFlags;
	public IRouter node;
	public IRouter parent;

	@Override
	public int compareTo(SearchNode o) {
		return this.distance-o.distance;
	}
}