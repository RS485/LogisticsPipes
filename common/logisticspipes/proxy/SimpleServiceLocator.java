/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy;

import java.util.LinkedList;

import logisticspipes.interfaces.SecurityStationManager;
import logisticspipes.interfaces.routing.ChannelConnectionManager;
import logisticspipes.interfaces.routing.ChannelManagerProvider;
import logisticspipes.logistics.LogisticsFluidManager;
import logisticspipes.logistics.LogisticsFluidManagerImpl;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logistics.LogisticsManagerImpl;
import logisticspipes.proxy.interfaces.CraftingRecipeProvider;
import logisticspipes.renderer.newpipe.GLRenderListHandler;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.RouterManagerImpl;
import logisticspipes.routing.channels.ChannelManagerProviderImpl;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.TankUtilFactory;

public final class SimpleServiceLocator {

	private SimpleServiceLocator() {}

	public static final ChannelConnectionManager connectionManager = RouterManagerImpl.INSTANCE;

	public static final SecurityStationManager securityStationManager = RouterManagerImpl.INSTANCE;

	public static final RouterManager routerManager = RouterManagerImpl.INSTANCE;

	public static final LogisticsManager logisticsManager = LogisticsManagerImpl.INSTANCE;

	public static final LogisticsFluidManager logisticsFluidManager = LogisticsFluidManagerImpl.INSTANCE;

	public static final InventoryUtilFactory inventoryUtilFactory = InventoryUtilFactory.INSTANCE;

	public static final TankUtilFactory tankUtilFactory = TankUtilFactory.INSTANCE;

	public static final LinkedList<CraftingRecipeProvider> craftingRecipeProviders = new LinkedList<>();

	public static final PipeInformationManager pipeInformationManager = PipeInformationManager.INSTANCE;

	public static final RoutedItemHelper routedItemHelper = RoutedItemHelper.INSTANCE;

	public static final GLRenderListHandler renderListHandler = GLRenderListHandler.INSTANCE;

	public static final ConfigToolHandler configToolHandler = ConfigToolHandler.INSTANCE;

	public static final ChannelManagerProvider channelManagerProvider = ChannelManagerProviderImpl.INSTANCE;

}
