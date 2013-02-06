/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;

/**
 * Defines direction with a cost
 */
public class ExitRoute implements Comparable<ExitRoute>{
	public ForgeDirection exitOrientation;
	public final ForgeDirection insertOrientation;
	public int metric;
	public EnumSet<PipeRoutingConnectionType> connectionDetails;
	public final IRouter destination;
	
	public ExitRoute(IRouter destination, ForgeDirection exitOrientation, ForgeDirection insertOrientation, int metric, EnumSet<PipeRoutingConnectionType> connectionDetails)
	{
		this.destination = destination;
		this.exitOrientation = exitOrientation;
		this.insertOrientation = insertOrientation;
		this.metric = metric;
		this.connectionDetails = connectionDetails;
	}

	public ExitRoute( ExitRoute other){
		this.destination = other.destination;
		this.exitOrientation = other.exitOrientation;
		this.insertOrientation = other.insertOrientation;
		this.metric = other.metric;
		this.connectionDetails = other.connectionDetails;
		
	}

	@Override public boolean equals(Object aThat) {
	    //check for self-comparison
	    if ( this == aThat ) return true;

	    if ( !(aThat instanceof ExitRoute) ) return false;
	    ExitRoute that = (ExitRoute)aThat;
		return this.exitOrientation.equals(that.exitOrientation) && 
				this.insertOrientation.equals(that.insertOrientation) && 
				this.connectionDetails.equals(that.connectionDetails) && 
				this.metric==that.metric;
	}
	
	public String toString() {
		return "{" + this.exitOrientation.name() + "," + this.insertOrientation.name() + "," + metric + ", ConnectionDetails: " + connectionDetails + "}";
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
		return this.metric - o.metric;
	}
}
