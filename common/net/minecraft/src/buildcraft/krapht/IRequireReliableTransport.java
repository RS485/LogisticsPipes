/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.krapht.ItemIdentifier;

public interface IRequireReliableTransport {
	public void itemLost(ItemIdentifier item);
	public void itemArrived(ItemIdentifier item);

}
