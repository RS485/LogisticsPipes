/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.krapht.forestry.IForestryProxy;
import net.minecraft.src.buildcraft.krapht.ic2.IElectricItemProxy;
import net.minecraft.src.buildcraft.krapht.logistics.ILogisticsManagerV2;
import net.minecraft.src.buildcraft.krapht.routing.IRouterManager;
import net.minecraft.src.buildcraft.krapht.recipeproviders.ICraftingRecipeProvider;
import net.minecraft.src.krapht.InventoryUtilFactory;

public final class SimpleServiceLocator {
	
	private SimpleServiceLocator(){};
	
	public static IBuildCraftProxy buildCraftProxy = null;
	public static void setBuildCraftProxy(final IBuildCraftProxy bcProxy){
		buildCraftProxy = bcProxy;
	}
	
	public static IElectricItemProxy electricItemProxy;
	public static  void setElectricItemProxy(final IElectricItemProxy fProxy){
		electricItemProxy = fProxy;
	}

	public static IForestryProxy forestryProxy;
	public static  void setForestryProxy(final IForestryProxy fProxy){
		forestryProxy = fProxy;
	}
	
	public static IRouterManager routerManager;
	public static void setRouterManager(final IRouterManager routerMngr){
		routerManager = routerMngr;
	}
	
	public static ILogisticsManagerV2 logisticsManager;
	public static  void setLogisticsManager(final ILogisticsManagerV2 logisticsMngr){
		logisticsManager = logisticsMngr;
	}
	
	public static InventoryUtilFactory inventoryUtilFactory;
	public static  void setInventoryUtilFactory(final InventoryUtilFactory invUtilFactory){
		inventoryUtilFactory = invUtilFactory;
	}
	
	public static LinkedList<ICraftingRecipeProvider> craftingRecipeProviders = new LinkedList<ICraftingRecipeProvider>();
	public static void addCraftingRecipeProvider(ICraftingRecipeProvider provider) {
		if (!craftingRecipeProviders.contains(provider)) {
			craftingRecipeProviders.add(provider);
		}
	}
	
}
