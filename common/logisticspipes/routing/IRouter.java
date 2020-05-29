/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.List;
import java.util.UUID;

import net.minecraft.util.EnumFacing;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.util.LPFinalSerializable;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface IRouter extends LPFinalSerializable {

	void destroy();

	void update(boolean doFullRefresh, CoreRoutedPipe pipe);

	boolean isRoutedExit(EnumFacing connection);

	boolean isSubPoweredExit(EnumFacing connection);

	int getDistanceToNextPowerPipe(EnumFacing dir);

	boolean hasRoute(int id, boolean active, ItemIdentifier type);

	ExitRoute getExitFor(int id, boolean active, ItemIdentifier type);

	List<List<ExitRoute>> getRouteTable();

	List<ExitRoute> getIRoutersByCost();

	CoreRoutedPipe getPipe();

	CoreRoutedPipe getCachedPipe();

	boolean isInDim(int dimension);

	boolean isAt(int dimension, int xCoord, int yCoord, int zCoord);

	UUID getId();

	LogisticsModule getLogisticsModule();

	void clearPipeCache();

	int getSimpleID();

	DoubleCoordinates getLPPosition();

	/* Automated Disconnection */
	boolean isSideDisconnected(EnumFacing dir);

	List<ExitRoute> getDistanceTo(IRouter r);

	void clearInterests();

	List<Pair<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider();

	List<Pair<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider();

	boolean isCacheInvalid();

	//force-update LSA version in the network
	void forceLsaUpdate();

	List<ExitRoute> getRoutersOnSide(EnumFacing direction);

	void queueTask(int i, IRouterQueuedTask callable);

	@Override
	default void write(LPDataOutput output) {
		output.writeSerializable(getLPPosition());
	}

}
