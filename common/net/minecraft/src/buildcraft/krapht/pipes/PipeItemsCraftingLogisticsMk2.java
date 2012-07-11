/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.krapht.CraftingTemplate;
import net.minecraft.src.buildcraft.krapht.ICraftItems;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsOrderManager;
import net.minecraft.src.buildcraft.krapht.LogisticsPromise;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.LogisticsTransaction;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.krapht.AdjacentTile;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.ItemIdentifierStack;
import net.minecraft.src.krapht.WorldUtil;

public class PipeItemsCraftingLogisticsMk2 extends PipeItemsCraftingLogistics{
	
	public PipeItemsCraftingLogisticsMk2(int itemID) {
		super(itemID);
	}
	
	private boolean combinable(ItemStack stack1, ItemStack stack2) {
		if(stack1 == null || stack2 == null) {
			return false;
		}
		return stack1.itemID == stack2.itemID && stack1.getItemDamage() == stack2.getItemDamage() && (stack1.stackSize + stack2.stackSize) < stack1.getMaxStackSize();
	}
	
	@Override
	protected ItemStack extractFromAutoWorkbench(TileAutoWorkbench workbench){
		ItemStack stack = workbench.extractItem(true, Orientations.Unknown);
		if(stack != null) {
			for(int i = 1;i < 64;i++) {
				if(_orderManager.getNextRequest().numberLeft() <= stack.stackSize) {
					break;
				}
				if(combinable(workbench.extractItem(false, Orientations.Unknown),stack)) {
					ItemStack stack2 = workbench.extractItem(true, Orientations.Unknown);
					stack.stackSize += stack2.stackSize;
				}
			}
		}
		return stack;
	}
	
	@Override
	protected ItemStack extractFromIInventory(IInventory inv){
		ItemStack items = null;
		for(int i=0; i < 64;i++) {
			if(items != null) {
				if(_orderManager.getNextRequest().numberLeft() <= items.stackSize) {
					break;
				}
			}
			InventoryUtil invUtil = new InventoryUtil(inv, false);
			LogicCrafting craftingLogic = (LogicCrafting) this.logic;
			ItemStack itemstack = craftingLogic.getCraftedItem();
			if (itemstack == null) return null;
		
			ItemIdentifierStack targetItemStack = ItemIdentifierStack.GetFromStack(itemstack);
			if(items == null) {
				items = invUtil.getSingleItem(targetItemStack.getItem());
				if(items == null) {
					break;
				}
			} else {
				if(invUtil.getSingleItem(targetItemStack.getItem())!= null) {
					items.stackSize++;
				} else {
					break;
				}
			}
		}
		return items;
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_CRAFTERMK2_TEXTURE;
	}
}
