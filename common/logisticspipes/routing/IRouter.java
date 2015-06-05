/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;

import net.minecraftforge.common.util.ForgeDirection;

public interface IRouter {

	public interface IRAction {

		public boolean isInteresting(IRouter that);

		public void doTo(IRouter that);
	}

	public void destroy();

	public void update(boolean doFullRefresh, CoreRoutedPipe pipe);

	public void updateInterests(); // calls getInterests on the attached pipe, and updates the global cache.

	public boolean isRoutedExit(ForgeDirection connection);

	public boolean isSubPoweredExit(ForgeDirection connection);

	public int getDistanceToNextPowerPipe(ForgeDirection dir);

	public boolean hasRoute(int id, boolean active, ItemIdentifier type);

	public ExitRoute getExitFor(int id, boolean active, ItemIdentifier type);

	public List<List<ExitRoute>> getRouteTable();

	public List<ExitRoute> getIRoutersByCost();

	public CoreRoutedPipe getPipe();

	public CoreRoutedPipe getCachedPipe();

	public boolean isInDim(int dimension);

	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord);

	public UUID getId();

	public LogisticsModule getLogisticsModule();

	public void clearPipeCache();

	public int getSimpleID();

	public LPPosition getLPPosition();

	/**
	 * @param hasBeenProcessed
	 *            a bitset flagging which nodes have already been acted on (the
	 *            router should set the bit for it's own id, then return true.
	 * @param actor
	 *            the visitor
	 * @return true if the bitset was cleared at some stage during the process,
	 *         resulting in a potentially incomplete bitset.
	 */
	public void act(BitSet hasBeenProcessed, IRAction actor);

	public void flagForRoutingUpdate();

	public boolean checkAdjacentUpdate();

	/* Automated Disconnection */
	public boolean isSideDisconneceted(ForgeDirection dir);

	public List<ExitRoute> getDistanceTo(IRouter r);

	public void clearInterests();

	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider();

	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider();

	public boolean isValidCache();

	//force-update LSA version in the network
	public void forceLsaUpdate();

	public List<ExitRoute> getRoutersOnSide(ForgeDirection direction);

	public int getDimension();

	public void queueTask(int i, IRouterQueuedTask callable);
}
