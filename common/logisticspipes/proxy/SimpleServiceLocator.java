/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy;

import java.util.LinkedList;

import logisticspipes.interfaces.routing.IDirectConnectionManager;
import logisticspipes.logistics.ILogisticsManagerV2;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IElectricItemProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import logisticspipes.proxy.specialconnection.SpecialConnection;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import logisticspipes.routing.IRouterManager;
import logisticspipes.ticks.ClientPacketBufferHandlerThread;
import logisticspipes.ticks.ServerPacketBufferHandlerThread;
import logisticspipes.utils.InventoryUtilFactory;

public final class SimpleServiceLocator {
	
	private SimpleServiceLocator(){};
	
	public static BuildCraftProxy buildCraftProxy = null;
	public static void setBuildCraftProxy(final BuildCraftProxy bcProxy){
		buildCraftProxy = bcProxy;
	}
	
	public static IElectricItemProxy electricItemProxy;
	public static void setElectricItemProxy(final IElectricItemProxy fProxy){
		electricItemProxy = fProxy;
	}

	public static IForestryProxy forestryProxy;
	public static  void setForestryProxy(final IForestryProxy fProxy){
		forestryProxy = fProxy;
	}
	
	public static ICCProxy ccProxy;
	public static  void setCCProxy(final ICCProxy cProxy){
		ccProxy = cProxy;
	}
	
	public static IDirectConnectionManager connectionManager;
	public static void setDirectConnectionManager(final IDirectConnectionManager conMngr){
		connectionManager = conMngr;
	}
	
	public static IRouterManager routerManager;
	public static void setRouterManager(final IRouterManager routerMngr){
		routerManager = routerMngr;
	}
	
	public static ILogisticsManagerV2 logisticsManager;
	public static void setLogisticsManager(final ILogisticsManagerV2 logisticsMngr){
		logisticsManager = logisticsMngr;
	}
	
	public static InventoryUtilFactory inventoryUtilFactory;
	public static void setInventoryUtilFactory(final InventoryUtilFactory invUtilFactory){
		inventoryUtilFactory = invUtilFactory;
	}
	
	public static LinkedList<ICraftingRecipeProvider> craftingRecipeProviders = new LinkedList<ICraftingRecipeProvider>();
	public static void addCraftingRecipeProvider(ICraftingRecipeProvider provider) {
		if (!craftingRecipeProviders.contains(provider)) {
			craftingRecipeProviders.add(provider);
		}
	}
	
	public static SpecialConnection specialconnection;
	public static void setSpecialConnectionHandler(final SpecialConnection special){
		specialconnection = special;
	}
	
	public static IThaumCraftProxy thaumCraftProxy;
	public static void setThaumCraftProxy(IThaumCraftProxy proxy) {
		thaumCraftProxy = proxy;
	}

	public static ClientPacketBufferHandlerThread clientBufferHandler;
	public static void setClientPacketBufferHandlerThread(ClientPacketBufferHandlerThread proxy) {
		clientBufferHandler = proxy;
	}
	
	public static ServerPacketBufferHandlerThread serverBufferHandler;
	public static void setServerPacketBufferHandlerThread(ServerPacketBufferHandlerThread proxy) {
		serverBufferHandler = proxy;
	}
}
