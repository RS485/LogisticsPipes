/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

class RouterCost {

	public final IRouter router;
	public final int cost;

	public RouterCost(IRouter router, int cost) {
		this.router = router;
		this.cost = cost;
	}

}
