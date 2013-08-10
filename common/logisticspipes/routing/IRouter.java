/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraftforge.common.ForgeDirection;

public interface IRouter {
	public interface IRAction {
		public boolean isInteresting(IRouter that);
		public boolean doTo(IRouter that);
	}
	public void destroy();
	public void update(boolean fullRefresh);
	public void updateInterests(); // calls getInterests on the attached pipe, and updates the global cache.
	
	public boolean isRoutedExit(ForgeDirection connection);
	public boolean hasRoute(int id);
	public ForgeDirection getExitFor(int id);
	
	public List<ExitRoute> getRouteTable();
	public List<ExitRoute> getIRoutersByCost();
	public CoreRoutedPipe getPipe();
	public CoreRoutedPipe getCachedPipe();
	public boolean isInDim(int dimension);
	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord);
	public UUID getId();
	
	public LogisticsModule getLogisticsModule();
	public void clearPipeCache();
	
	public IRouter getRouter(ForgeDirection insertOrientation);
	public int getSimpleID();

	/**
	 * 
	 * @param hasBeenProcessed a bitset flagging which nodes have already been acted on 
	 * (the router should set the bit for it's own id, then return true.
	 * @param actor
	 * the visitor
	 * @return true if the bitset was cleared at some stage during the process, resulting in a potentially incomplete bitset.
	 */
	public boolean act(BitSet hasBeenProcessed, IRAction actor);
	public void flagForRoutingUpdate();
	public boolean checkAdjacentUpdate();
	
	/* Automated Disconnection */
	public boolean isSideDisconneceted(ForgeDirection dir);
	public ExitRoute getDistanceTo(IRouter r);
	
	public void clearInterests();
	public List<ILogisticsPowerProvider> getPowerProvider();
	public List<IRouter> getFilteringRouter();
	
	public boolean isValidCache();
}
