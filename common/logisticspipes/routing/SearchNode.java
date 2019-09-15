package logisticspipes.routing;

import java.util.EnumSet;

public class SearchNode implements Comparable<SearchNode> {

	public int distance;
	private final EnumSet<PipeRoutingConnectionType> connectionFlags;
	public final Router node;
	public Router root;

	public SearchNode(Router r, int d, EnumSet<PipeRoutingConnectionType> enumSet, Router p) {
		distance = d;
		connectionFlags = enumSet;
		node = r;
		root = p;
	}

	public boolean hasActivePipe() {
		return node != null && node.getCachedPipe() != null;
	}

	// copies
	public EnumSet<PipeRoutingConnectionType> getFlags() {
		return EnumSet.copyOf(connectionFlags);
	}

	@Override
	public int compareTo(SearchNode o) {
		return distance - o.distance;
	}

	public void removeFlags(EnumSet<PipeRoutingConnectionType> flags) {
		connectionFlags.removeAll(flags);
	}

	public boolean containsFlag(PipeRoutingConnectionType flag) {
		return connectionFlags.contains(flag);
	}
}
