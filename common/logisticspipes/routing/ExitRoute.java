/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import logisticspipes.interfaces.routing.IFilter;
import net.minecraftforge.common.ForgeDirection;

/**
 * Defines direction with a cost
 */
public class ExitRoute implements Comparable<ExitRoute>{
	public ForgeDirection exitOrientation;
	public ForgeDirection insertOrientation;
	public int distanceToDestination;
	public final int destinationDistanceToRoot;
	public final EnumSet<PipeRoutingConnectionType> connectionDetails;
	public final IRouter destination;
	public IRouter root;
	public List<IFilter> filters = Collections.unmodifiableList(new ArrayList<IFilter>(0));
	
	public ExitRoute(IRouter source, IRouter destination, ForgeDirection exitOrientation, ForgeDirection insertOrientation, int metric, EnumSet<PipeRoutingConnectionType> connectionDetails) {
		this.destination = destination;
		this.root = source;
		this.exitOrientation = exitOrientation;
		this.insertOrientation = insertOrientation;
		this.connectionDetails = connectionDetails;
		if(connectionDetails.contains(PipeRoutingConnectionType.canRouteTo)) {
			this.distanceToDestination=metric;
		} else {
			this.distanceToDestination=Integer.MAX_VALUE;
		}
		if(connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom)) {
			this.destinationDistanceToRoot=metric;
		} else {
			this.destinationDistanceToRoot=Integer.MAX_VALUE;
		}
	}

	@Override
	public boolean equals(Object aThat) {
	    //check for self-comparison
	    if ( this == aThat ) return true;

	    if ( !(aThat instanceof ExitRoute) ) return false;
	    ExitRoute that = (ExitRoute)aThat;
		return this.exitOrientation.equals(that.exitOrientation) && 
				this.insertOrientation.equals(that.insertOrientation) && 
				this.connectionDetails.equals(that.connectionDetails) && 
				this.distanceToDestination==that.distanceToDestination && 
				this.destinationDistanceToRoot==that.destinationDistanceToRoot && 
				this.filters.equals(that.filters);
	}
	
	@Override
	public String toString() {
		return "{" + this.exitOrientation.name() + "," + this.insertOrientation.name() + "," + distanceToDestination +  "," + destinationDistanceToRoot + ", ConnectionDetails: " + connectionDetails + ", " + this.filters + "}";
	}

	public void removeFlags(EnumSet<PipeRoutingConnectionType> flags) {
		connectionDetails.removeAll(flags);		
	}

	public boolean containsFlag(PipeRoutingConnectionType flag) {
		return connectionDetails.contains(flag);
	}

	public boolean hasActivePipe(){
		return destination!=null && destination.getCachedPipe()!=null;
	}
	
	//copies
	public EnumSet<PipeRoutingConnectionType> getFlags() {
		return EnumSet.copyOf(connectionDetails);
	}

	@Override
	public int compareTo(ExitRoute o) {
		int c = this.distanceToDestination - o.distanceToDestination;
		if (c==0) return this.destination.getSimpleID() - o.destination.getSimpleID();
		return c;
	}

	public ExitRoute(IRouter source, IRouter destination, int distance, EnumSet<PipeRoutingConnectionType> enumSet, List<IFilter> filterA, List<IFilter> filterB) {
		this(source, destination, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, distance, enumSet);
		List<IFilter> filter = new ArrayList<IFilter>(filterA.size() + filterB.size());
		filter.addAll(filterA);
		filter.addAll(filterB);
		this.filters = Collections.unmodifiableList(filter);
	}
}