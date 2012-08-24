/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht.logistics;

import java.util.LinkedList;
import java.util.UUID;

import logisticspipes.buildcraft.krapht.PipeTransportLogistics;
import logisticspipes.buildcraft.logisticspipes.IRoutedItem;
import logisticspipes.krapht.ItemIdentifier;


import net.minecraft.src.ItemStack;

public interface ILogisticsManagerV2 {
	
	public IRoutedItem assignDestinationFor(IRoutedItem item, UUID sourceRouterUUID, boolean excludeSource);
	public IRoutedItem destinationUnreachable(IRoutedItem item, UUID currentRouter);
	boolean hasDestination(ItemStack stack, boolean allowDefault, UUID sourceRouter, boolean excludeSource);
	
}
