/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import logisticspipes.interfaces.routing.IFluidProvider;
import logisticspipes.utils.FluidIdentifier;

public class FluidLogisticsPromise {
	public FluidIdentifier liquid;
	public int amount;
	public IFluidProvider sender;
	
	public FluidLogisticsPromise copy() {
		FluidLogisticsPromise result = new FluidLogisticsPromise();
		result.liquid = liquid;
		result.amount = amount;
		result.sender = sender;
		return result;
	}
}
