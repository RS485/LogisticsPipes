/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;

public interface IRouter {
	public void destroy();
	public void update(boolean fullRefresh);
	public void sendRoutedItem(ItemStack item, IRouter destination, Position origin);
	public boolean isRoutedExit(ForgeDirection connection);
	public boolean hasRoute(UUID id);
	public ForgeDirection getExitFor(UUID id);
	
	@Deprecated
	public HashMap<IRouter, ForgeDirection> getRouteTable();
	public List<IRouter> getIRoutersByCost();
	public CoreRoutedPipe getPipe();
	
	public UUID getId();
	public void itemDropped(RoutedEntityItem routedEntityItem);
	@Deprecated
	public void displayRoutes();
	@Deprecated
	public void displayRouteTo(IRouter r);
	@Deprecated
	public void inboundItemArrived(RoutedEntityItem routedEntityItem);
	
	public ILogisticsModule getLogisticsModule();
}
