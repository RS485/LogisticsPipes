/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht;

import logisticspipes.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;
import buildcraft.transport.*;
import buildcraft.transport.PipeTransportItems.*;
import buildcraft.transport.TileGenericPipe;

public interface IBuildCraftProxy {
	public boolean checkPipesConnections(TileEntity tile1, TileEntity tile2);
	public void dropItems (World world, IInventory inventory, int x, int y, int z);
	public void dropItems (World world, ItemStack stack, int x, int y, int z);
	public IRoutedItem GetOrCreateRoutedItem(World worldObj, EntityData itemData);
	public boolean isRoutedItem(IPipedItem item);
	public IRoutedItem GetRoutedItem(IPipedItem item);
	
	public IRoutedItem CreateRoutedItem(World worldObj, IPipedItem item);
	public IRoutedItem CreateRoutedItem(ItemStack payload, World worldObj);
}
