/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.proxy.interfaces.IBuildCraftProxy;
import logisticspipes.routing.RoutedEntityItem;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.core.EntityPassiveItem;
import buildcraft.api.core.Orientations;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.TileGenericPipe;

public class BuildCraftProxy3 implements IBuildCraftProxy{

	@Override
	public boolean checkPipesConnections(TileEntity tile1, TileEntity tile2) {
		return Utils.checkPipesConnections(tile1, tile2);
	}

	@Override
	public void dropItems(World world, IInventory inventory, int x, int y, int z) {
		Utils.dropItems(world, inventory, x, y, z);
	}

	@Override
	public void dropItems(World world, ItemStack stack, int x, int y, int z) {
		Utils.dropItems(world, stack, x, y, z);
	}

	@Override
	public IRoutedItem GetOrCreateRoutedItem(World worldObj, EntityData itemData) {
		if (!isRoutedItem(itemData.item)){
			RoutedEntityItem newItem = new RoutedEntityItem(worldObj, itemData.item);
			itemData.item = newItem;
			return newItem;
		}
		return (IRoutedItem) itemData.item; 
	}
	
	@Override
	public boolean isRoutedItem(IPipedItem item) {
		return (item instanceof RoutedEntityItem);
	}
	
	@Override
	public IRoutedItem GetRoutedItem(IPipedItem item) {
		return (IRoutedItem) item;
	}
	

	@Override
	public IRoutedItem CreateRoutedItem(World worldObj, IPipedItem item) {
		RoutedEntityItem newItem = new RoutedEntityItem(worldObj, item);
		return newItem;
	}

	@Override
	public IRoutedItem CreateRoutedItem(ItemStack payload, World worldObj) {
		EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, 0, 0, 0, payload);
		return CreateRoutedItem(worldObj, entityItem);
	}
}
