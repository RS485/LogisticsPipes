/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.transport.*;
import net.minecraft.src.buildcraft.transport.PipeTransportItems.*;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public interface IBuildCraftProxy {
	public boolean checkPipesConnections(TileEntity tile1, TileEntity tile2);
	public void dropItems (World world, IInventory inventory, int x, int y, int z);
	public void dropItems (World world, ItemStack stack, int x, int y, int z);
	public IRoutedItem GetOrCreateRoutedItem(World worldObj, EntityData itemData);
	public boolean isRoutedItem(EntityPassiveItem item);
	public IRoutedItem GetRoutedItem(EntityPassiveItem item);
	
	public IRoutedItem CreateRoutedItem(World worldObj, EntityPassiveItem item);
	public IRoutedItem CreateRoutedItem(ItemStack payload, World worldObj);
}
