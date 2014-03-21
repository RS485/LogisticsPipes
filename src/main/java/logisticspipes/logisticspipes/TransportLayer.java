/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import net.minecraftforge.common.ForgeDirection;

/**
 * This class is responsible for handling items arriving at its destination
 * 
 * @author Krapht
 *
 */
public abstract class TransportLayer {
	
	public abstract boolean stillWantItem(IRoutedItem item);
	public abstract ForgeDirection itemArrived(IRoutedItem item, ForgeDirection denyed);
	
//	public void SendItem(TravelingItem data){
//		
//	}
//	
//	public void ItemLost(TravelingItem data){
//		
//	}

}
