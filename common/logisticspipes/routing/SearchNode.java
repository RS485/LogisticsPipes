package logisticspipes.routing;

import java.util.EnumSet;

public class SearchNode implements Comparable<SearchNode>{
	public SearchNode(IRouter r,int d, EnumSet<PipeRoutingConnectionType> enumSet,IRouter p){
		distance=d;
		connectionFlags=enumSet;
		node=r;
		root=p;
	}
	public boolean hasActivePipe(){
		return node!=null && node.getPipe()!=null;
	}
	public int distance;
	private final EnumSet<PipeRoutingConnectionType> connectionFlags;
	public final IRouter node;
	public IRouter root;
	private final int ROUTING_PENALITY=10000;
	//copies
	public EnumSet<PipeRoutingConnectionType> getFlags() {
		return EnumSet.copyOf(connectionFlags);
	}

	@Override
	public int compareTo(SearchNode o) {
		int delta=0;
		if(EnumSet.copyOf(connectionFlags).removeAll(ServerRouter.blocksItems))
			delta+=ROUTING_PENALITY;
		if(EnumSet.copyOf(o.connectionFlags).removeAll(ServerRouter.blocksItems))
			delta-=ROUTING_PENALITY;
		
		return this.distance-o.distance+delta;
	}

	public void removeFlags(EnumSet<PipeRoutingConnectionType> flags) {
		connectionFlags.removeAll(flags);		
	}

	public boolean containsFlag(PipeRoutingConnectionType flag) {
		return connectionFlags.contains(flag);
	}
}