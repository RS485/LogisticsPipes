/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import logisticspipes.buildcraft.krapht.routing.Router;
import logisticspipes.krapht.ItemIdentifier;



@Deprecated
public interface ILogisticsManager {
//	@Deprecated
//	public UUID getDestinationFor(ItemIdentifier item, Set<Router> validDestinations);
	@Deprecated
	public HashMap<ItemIdentifier, Integer> getAvailableItems(Set<Router> validDestinations);
	@Deprecated
	public LinkedList<ItemIdentifier> getCraftableItems(Set<Router> validDestinations);
}