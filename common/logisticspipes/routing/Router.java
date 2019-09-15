/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.util.LPFinalSerializable;

public interface Router extends LPFinalSerializable {

	void destroy();

	void update(boolean doFullRefresh, CoreRoutedPipe pipe);

	void updateInterests(); // calls getInterests on the attached pipe, and updates the global cache.

	boolean isRoutedExit(Direction connection);

	boolean isSubPoweredExit(Direction connection);

	int getDistanceToNextPowerPipe(Direction dir);

	boolean hasRoute(UUID id, boolean active, ItemStack stack);

	ExitRoute getExitFor(UUID id, boolean active, ItemStack stack);

	List<List<ExitRoute>> getRouteTable();

	List<ExitRoute> getIRoutersByCost();

	CoreRoutedPipe getPipe();

	CoreRoutedPipe getCachedPipe();

	boolean isInDim(World world);

	boolean isAt(World world, BlockPos pos);

	UUID getId();

	@Deprecated
	int getSimpleId();

	LogisticsModule getLogisticsModule();

	void clearPipeCache();

	BlockPos getPos();

	/**
	 * @param hasBeenProcessed a bitset flagging which nodes have already been acted on (the
	 *                         router should set the bit for it's own id, then return true.
	 * @param actor            the visitor
	 * @return true if the bitset was cleared at some stage during the process,
	 * resulting in a potentially incomplete bitset.
	 */
	void act(BitSet hasBeenProcessed, IRAction actor);

	void flagForRoutingUpdate();

	boolean checkAdjacentUpdate();

	/* Automated Disconnection */
	boolean isSideDisconnected(Direction dir);

	List<ExitRoute> getDistanceTo(Router r);

	void clearInterests();

	List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider();

	List<Tuple2<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider();

	boolean isValidCache();

	//force-update LSA version in the network
	void forceLsaUpdate();

	List<ExitRoute> getRoutersOnSide(Direction direction);

	World getWorld();

	void queueTask(int ticks, RouterQueuedTask callable);

	default void write(PacketByteBuf buf) {
		buf.writeBlockPos(getPos());
	}

	interface IRAction {

		boolean isInteresting(Router that);

		void doTo(Router that);

	}

}
