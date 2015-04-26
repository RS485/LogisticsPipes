/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy;

import java.util.LinkedList;

import logisticspipes.interfaces.ISecurityStationManager;
import logisticspipes.interfaces.routing.IDirectConnectionManager;
import logisticspipes.logistics.ILogisticsFluidManager;
import logisticspipes.logistics.ILogisticsManager;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.IBetterStorageProxy;
import logisticspipes.proxy.interfaces.IBinnieProxy;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.proxy.interfaces.ICoFHPowerProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IEnderIOProxy;
import logisticspipes.proxy.interfaces.IEnderStorageProxy;
import logisticspipes.proxy.interfaces.IExtraCellsProxy;
import logisticspipes.proxy.interfaces.IFactorizationProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.proxy.interfaces.IIronChestProxy;
import logisticspipes.proxy.interfaces.INEIProxy;
import logisticspipes.proxy.interfaces.IOpenComputersProxy;
import logisticspipes.proxy.interfaces.ITDProxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import logisticspipes.proxy.interfaces.IToolWrenchProxy;
import logisticspipes.proxy.progressprovider.MachineProgressProvider;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.proxy.specialtankhandler.SpecialTankHandler;
import logisticspipes.recipes.CraftingPermissionManager;
import logisticspipes.renderer.newpipe.GLRenderListHandler;
import logisticspipes.routing.IRouterManager;
import logisticspipes.routing.pathfinder.PipeInformaitonManager;
import logisticspipes.ticks.ClientPacketBufferHandlerThread;
import logisticspipes.ticks.ServerPacketBufferHandlerThread;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;

public final class SimpleServiceLocator {
	
	private SimpleServiceLocator(){};
	
	public static IBCProxy buildCraftProxy = null;
	public static void setBuildCraftProxy(final IBCProxy bcProxy){
		buildCraftProxy = bcProxy;
	}
	
	public static IIC2Proxy IC2Proxy;
	public static void setElectricItemProxy(final IIC2Proxy ic2Proxy){
		IC2Proxy = ic2Proxy;
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
	
	public static ISecurityStationManager securityStationManager;
	public static void setSecurityStationManager(final ISecurityStationManager secStationMngr){
		securityStationManager = secStationMngr;
	}
	
	public static IRouterManager routerManager;
	public static void setRouterManager(final IRouterManager routerMngr){
		routerManager = routerMngr;
	}
	
	public static ILogisticsManager logisticsManager;
	public static void setLogisticsManager(final ILogisticsManager logisticsMngr){
		logisticsManager = logisticsMngr;
	}
	
	public static ILogisticsFluidManager logisticsFluidManager;
	public static void setLogisticsFluidManager(final ILogisticsFluidManager logisticsMngr){
		logisticsFluidManager = logisticsMngr;
	}
	
	public static InventoryUtilFactory inventoryUtilFactory;
	public static void setInventoryUtilFactory(final InventoryUtilFactory invUtilFactory){
		inventoryUtilFactory = invUtilFactory;
	}
	
	public static LinkedList<ICraftingRecipeProvider> craftingRecipeProviders = new LinkedList<ICraftingRecipeProvider>();
	public static void addCraftingRecipeProvider(ICraftingRecipeProvider provider) {
		craftingRecipeProviders.add(provider);
	}

	public static SpecialPipeConnection specialpipeconnection;
	public static void setSpecialConnectionHandler(final SpecialPipeConnection special){
		specialpipeconnection = special;
	}
	
	public static SpecialTileConnection specialtileconnection;
	public static void setSpecialConnectionHandler(final SpecialTileConnection special){
		specialtileconnection = special;
	}
	
	public static IThaumCraftProxy thaumCraftProxy;
	public static void setThaumCraftProxy(IThaumCraftProxy proxy) {
		thaumCraftProxy = proxy;
	}
	
	public static IThermalExpansionProxy thermalExpansionProxy;
	public static void setThermalExpansionProxy(IThermalExpansionProxy proxy) {
		thermalExpansionProxy = proxy;
	}
	
	public static IBetterStorageProxy betterStorageProxy;
	public static void setBetterStorageProxy(IBetterStorageProxy proxy) {
		betterStorageProxy = proxy;
	}
	
	public static SpecialTankHandler specialTankHandler;
	public static void setSpecialTankHandler(SpecialTankHandler proxy) {
		specialTankHandler = proxy;
	}

	public static ClientPacketBufferHandlerThread clientBufferHandler;
	public static void setClientPacketBufferHandlerThread(ClientPacketBufferHandlerThread proxy) {
		clientBufferHandler = proxy;
	}
	
	public static ServerPacketBufferHandlerThread serverBufferHandler;
	public static void setServerPacketBufferHandlerThread(ServerPacketBufferHandlerThread proxy) {
		serverBufferHandler = proxy;
	}
	
	public static INEIProxy neiProxy;
	public static void setNEIProxy(INEIProxy proxy) {
		neiProxy = proxy;
	}
	
	public static CraftingPermissionManager craftingPermissionManager;
	public static void setCraftingPermissionManager(CraftingPermissionManager manager) {
		craftingPermissionManager = manager;
	}
	
	public static IFactorizationProxy factorizationProxy;
	public static void setFactorizationProxy(IFactorizationProxy proxy) {
		factorizationProxy = proxy;
	}
	
	public static PipeInformaitonManager pipeInformaitonManager;
	public static void setPipeInformationManager(PipeInformaitonManager manager) {
		pipeInformaitonManager = manager;
	}
	
	public static IEnderIOProxy enderIOProxy;
	public static void setEnderIOProxy(IEnderIOProxy proxy) {
		enderIOProxy = proxy;
	}
	
	public static IIronChestProxy ironChestProxy;
	public static void setIronChestProxy(IIronChestProxy proxy) {
		ironChestProxy = proxy;
	}
	
	public static IEnderStorageProxy enderStorageProxy;
	public static void setEnderStorageProxy(IEnderStorageProxy proxy) {
		enderStorageProxy = proxy;
	}
	
	public static MachineProgressProvider machineProgressProvider;
	public static void setMachineProgressProvider(MachineProgressProvider provider) {
		machineProgressProvider = provider;
	}
	
	public static RoutedItemHelper routedItemHelper;
	public static void setRoutedItemHelper(RoutedItemHelper helper) {
		routedItemHelper = helper;
	}

	public static IOpenComputersProxy openComputersProxy;
	public static void setOpenComputersProxy(IOpenComputersProxy proxy) {
		openComputersProxy = proxy;
	}
	
	public static IToolWrenchProxy toolWrenchHandler;
	public static void setToolWrenchProxy(IToolWrenchProxy handler) {
		toolWrenchHandler = handler;
	}

	public static GLRenderListHandler renderListHandler;
	public static void setRenderListHandler(GLRenderListHandler handler) {
		renderListHandler = handler;
	}
	
	public static IExtraCellsProxy extraCellsProxy;
	public static void setExtraCellsProxy(IExtraCellsProxy proxy) {
		extraCellsProxy = proxy;
	}
	
	public static ICoFHPowerProxy cofhPowerProxy;
	public static void setCoFHPowerProxy(ICoFHPowerProxy proxy) {
		cofhPowerProxy = proxy;
	}
	
	public static ITDProxy thermalDynamicsProxy;
	public static void setThermalDynamicsProxy(ITDProxy proxy) {
		thermalDynamicsProxy = proxy;
	}
	
	public static IBinnieProxy binnieProxy;
	public static void setBinnieProxy(IBinnieProxy proxy) {
		binnieProxy = proxy;
	}
}
