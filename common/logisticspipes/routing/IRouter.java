/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IPowerRouter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;

public interface IRouter extends IPowerRouter {
	public void destroy();
	public void update(boolean fullRefresh);
	public void sendRoutedItem(ItemStack item, IRouter destination, Position origin);
	public boolean isRoutedExit(ForgeDirection connection);
	public boolean hasRoute(int id);
	public ForgeDirection getExitFor(int id);
	
	public HashMap<IRouter, Pair<ForgeDirection, ForgeDirection>> getRouteTable();
	public List<SearchNode> getIRoutersByCost();
	public CoreRoutedPipe getPipe();
	
	//public UUID getId();
	public void itemDropped(RoutedEntityItem routedEntityItem);
	@Deprecated
	public void displayRoutes();
	@Deprecated
	public void displayRouteTo(IRouter r);
	public void inboundItemArrived(RoutedEntityItem routedEntityItem);
	
	public ILogisticsModule getLogisticsModule();
	public void clearPipeCache();
	
	public int getSimpleID();
}
