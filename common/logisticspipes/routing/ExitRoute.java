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
import java.util.Set;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.routing.debug.ExitRouteDebug;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Defines direction with a cost
 */
public class ExitRoute implements Comparable<ExitRoute> {

	public ForgeDirection exitOrientation;
	public ForgeDirection insertOrientation;
	public double distanceToDestination;
	public final double destinationDistanceToRoot;
	public final int blockDistance;
	public final EnumSet<PipeRoutingConnectionType> connectionDetails;
	public final IRouter destination;
	public IRouter root;
	public List<IFilter> filters = Collections.unmodifiableList(new ArrayList<IFilter>(0));
	/**
	 * Used to store debug information. No use in the actual Routing table
	 * calculation
	 */
	public ExitRouteDebug debug = new ExitRouteDebug();

	public ExitRoute(IRouter source, IRouter destination, ForgeDirection exitOrientation, ForgeDirection insertOrientation, double metric, EnumSet<PipeRoutingConnectionType> connectionDetails, int blockDistance) {
		this.destination = destination;
		root = source;
		this.exitOrientation = exitOrientation;
		this.insertOrientation = insertOrientation;
		this.connectionDetails = connectionDetails;
		if (connectionDetails.contains(PipeRoutingConnectionType.canRouteTo)) {
			distanceToDestination = metric;
		} else {
			distanceToDestination = Integer.MAX_VALUE;
		}
		if (connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom)) {
			destinationDistanceToRoot = metric;
		} else {
			destinationDistanceToRoot = Integer.MAX_VALUE;
		}
		this.blockDistance = blockDistance;
	}

	@Override
	public boolean equals(Object aThat) {
		//check for self-comparison
		if (this == aThat) {
			return true;
		}

		if (!(aThat instanceof ExitRoute)) {
			return false;
		}
		ExitRoute that = (ExitRoute) aThat;
		return exitOrientation.equals(that.exitOrientation) && insertOrientation.equals(that.insertOrientation) && connectionDetails.equals(that.connectionDetails) && distanceToDestination == that.distanceToDestination && destinationDistanceToRoot == that.destinationDistanceToRoot && destination == that.destination
				&& filters.equals(that.filters);
	}

	public boolean isSameWay(ExitRoute that) {
		if (equals(that)) {
			return true;
		}
		return connectionDetails.equals(that.connectionDetails) && destination == that.destination && filters.equals(that.filters);
	}

	@Override
	public String toString() {
		return "{" + exitOrientation.name() + "," + insertOrientation.name() + "," + distanceToDestination + "," + destinationDistanceToRoot + ", ConnectionDetails: " + connectionDetails + ", " + filters + "}";
	}

	public void removeFlags(EnumSet<PipeRoutingConnectionType> flags) {
		connectionDetails.removeAll(flags);
	}

	public boolean containsFlag(PipeRoutingConnectionType flag) {
		return connectionDetails.contains(flag);
	}

	public boolean hasActivePipe() {
		return destination != null && destination.getCachedPipe() != null;
	}

	//copies
	public EnumSet<PipeRoutingConnectionType> getFlags() {
		return EnumSet.copyOf(connectionDetails);
	}

	// Doesn't copy
	public Set<PipeRoutingConnectionType> getFlagsNoCopy() {
		return Collections.unmodifiableSet(connectionDetails);
	}

	@Override
	public int compareTo(ExitRoute o) {
		int c = (int) Math.floor(distanceToDestination - o.distanceToDestination);
		if (c == 0) {
			return destination.getSimpleID() - o.destination.getSimpleID();
		}
		return c;
	}

	public ExitRoute(IRouter source, IRouter destination, double distance, EnumSet<PipeRoutingConnectionType> enumSet, List<IFilter> filterA, List<IFilter> filterB, int blockDistance) {
		this(source, destination, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, distance, enumSet, blockDistance);
		List<IFilter> filter = new ArrayList<IFilter>(filterA.size() + filterB.size());
		filter.addAll(filterA);
		filter.addAll(filterB);
		filters = Collections.unmodifiableList(filter);
	}
}
