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
public class ExitRoute{
	public ForgeDirection exitOrientation;
	public int metric;
	public EnumSet<PipeRoutingConnectionType> connectionDetails;
	
	public ExitRoute(ForgeDirection exitOrientation, int metric, EnumSet<PipeRoutingConnectionType> connectionDetails)
	{
		this.exitOrientation = exitOrientation;
		this.metric = metric;
		this.connectionDetails = connectionDetails;
	}


	@Override public boolean equals(Object aThat) {
	    //check for self-comparison
	    if ( this == aThat ) return true;

	    if ( !(aThat instanceof ExitRoute) ) return false;
	    ExitRoute that = (ExitRoute)aThat;
		return this.exitOrientation.equals(that.exitOrientation) && 
				this.connectionDetails.equals(that.connectionDetails) && 
				this.metric==that.metric;
	}
	
	public String toString() {
		return "{" + this.exitOrientation.name() + "," + metric + ", ConnectionDetails: " + connectionDetails + "}";
	}
}
