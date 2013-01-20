/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.List;

import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.utils.ItemIdentifier;

public class LogisticsPromise {
	public ItemIdentifier item;
	public int numberOfItems;
	public IProvideItems sender;
	public List<IRelayItem> relayPoints;
	
	public LogisticsPromise copy() {
		LogisticsPromise result = new LogisticsPromise();
		result.item = item;
		result.numberOfItems = numberOfItems;
		result.sender = sender;
		result.relayPoints = relayPoints;
		return result;
	}
}
