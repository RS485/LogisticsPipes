/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.logisticspipes;

import buildcraft.api.core.Orientations;

/**
 * This class is responsible for handling items arriving at its destination
 * 
 * @author Krapht
 *
 */
public abstract class TransportLayer {
	
	public abstract boolean stillWantItem(IRoutedItem item);
	public abstract Orientations itemArrived(IRoutedItem item);
	
//	public void SendItem(EntityData data){
//		
//	}
//	
//	public void ItemLost(EntityData data){
//		
//	}

}
