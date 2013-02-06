package logisticspipes.routing;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;

public class SearchNode extends ExitRoute implements Comparable<ExitRoute> {
	
	public IRouter root;
	
	public SearchNode(IRouter r, int d, EnumSet<PipeRoutingConnectionType> enumSet, IRouter p) {
		super(r,ForgeDirection.UNKNOWN,ForgeDirection.UNKNOWN,d,enumSet);
		root = p;
	}
	
	public SearchNode(IRouter r, int d, EnumSet<PipeRoutingConnectionType> enumSet, IRouter p,ForgeDirection exit,ForgeDirection enter) {
		super(r,exit,enter,d,enumSet);
		root = p;
	}
}