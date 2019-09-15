package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class ClientRouter implements Router {

	private final BlockPos pos;

	public ClientRouter(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void destroy() {}

	@Override
	public int getSimpleId() {
		return -1;
	}

	@Override
	public void update(boolean doFullRefresh, CoreRoutedPipe pipe) {}

	@Override
	public boolean isRoutedExit(Direction connection) {
		throw new UnsupportedOperationException("Can't route on the client");
	}

	@Override
	public boolean hasRoute(int id, boolean flag, ItemStack stack) {
		throw new UnsupportedOperationException("Can't route on the client");
	}

	@Override
	public ExitRoute getExitFor(int id, boolean flag, ItemStack stack) {
		throw new UnsupportedOperationException("Can't route on the client");
	}

	@Override
	public ArrayList<List<ExitRoute>> getRouteTable() {
		throw new UnsupportedOperationException("Can't route on the client");
	}

	@Override
	public List<ExitRoute> getIRoutersByCost() {
		throw new UnsupportedOperationException("Can't route on the client");
	}

	@Override
	public CoreRoutedPipe getPipe() {
		World world = MinecraftClient.getInstance().world;

		if (world == null) return null;

		BlockEntity tile = world.getBlockEntity(pos);

		if (!(tile instanceof LogisticsTileGenericPipe)) return null;

		LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) tile;
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		return (CoreRoutedPipe) pipe.pipe;
	}

	@Override
	public CoreRoutedPipe getCachedPipe() {
		return getPipe();
	}

	@Override
	public boolean isInDim(int dimension) {
		return true;
	}

	@Override
	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord) {
		return _xCoord == xCoord && _yCoord == yCoord && _zCoord == zCoord;
	}

	@Override
	public DoubleCoordinates getLPPosition() {
		return new DoubleCoordinates(_xCoord, _yCoord, _zCoord);
	}

	@Override
	public UUID getId() {
		return UUID.randomUUID();
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		CoreRoutedPipe pipe = getPipe();
		if (pipe == null) {
			return null;
		}
		return pipe.getLogisticsModule();
	}

	@Override
	public void clearPipeCache() {
		// Not On Client Side
	}

	@Override
	public List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider() {
		return null;
	}

	@Override
	public void act(BitSet hasBeenProcessed, IRAction actor) {

	}

	@Override
	public void flagForRoutingUpdate() {

	}

	@Override
	public boolean checkAdjacentUpdate() {
		return false;
	}

	@Override
	public boolean isSideDisconnected(Direction dir) {
		return false;
	}

	@Override
	public void updateInterests() {

	}

	@Override
	public List<ExitRoute> getDistanceTo(Router r) {
		return null;
	}

	@Override
	public void clearInterests() {

	}

	@Override
	public boolean isValidCache() {
		return true;
	}

	@Override
	public void forceLsaUpdate() {}

	@Override
	public boolean isSubPoweredExit(Direction connection) {
		return false;
	}

	@Override
	public List<Tuple2<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider() {
		return null;
	}

	@Override
	public String toString() {
		return String.format("ServerRouter: {UUID: %s, AT: (%d, %d, %d)}", getId(), _xCoord, _yCoord, _zCoord);
	}

	@Override
	public List<ExitRoute> getRoutersOnSide(Direction exitOrientation) {
		return null;
	}

	@Override
	public int getDimension() {
		return 0;
	}

	@Override
	public int getDistanceToNextPowerPipe(Direction dir) {
		return 0;
	}

	@Override
	public void queueTask(int ticks, RouterQueuedTask callable) {}
}
