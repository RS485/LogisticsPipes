/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import buildcraft.api.core.Orientations;

/**
 * Defines direction with a cost
 */
class ExitRoute {
	public Orientations exitOrientation;
	public int metric;
	
	public ExitRoute(Orientations exitOrientation, int metric)
	{
		this.exitOrientation = exitOrientation;
		this.metric = metric;
	}
}
