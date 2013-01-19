/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifierStack;

public interface IRequestItems extends Comparable<IRequestItems>{
	public IRouter getRouter();
	public void itemCouldNotBeSend(ItemIdentifierStack item);
	public boolean useEnergy(int amount);
	public boolean useEnergy(int amount, boolean flag);
	public int getID();
	
	// only probably unique, but roughly in the range of 1-NumberOfLPPipes
	public int getSimpleID();
	public int getBiggestID();
	
	@Override
	public int compareTo(IRequestItems value2);
}
