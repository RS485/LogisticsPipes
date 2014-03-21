/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IRequireReliableTransport {
	public void itemLost(ItemIdentifierStack item);
	public void itemArrived(ItemIdentifierStack item);

}
