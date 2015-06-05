package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LPConstants;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class ClientRouter implements IRouter {

	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;

	public ClientRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		_xCoord = xCoord;
		_yCoord = yCoord;
		_zCoord = zCoord;
	}

	@Override
	public void destroy() {}

	@Override
	public int getSimpleID() {
		return -420;
	}

	@Override
	public void update(boolean doFullRefresh, CoreRoutedPipe pipe) {

	}

	@Override
	public boolean isRoutedExit(ForgeDirection connection) {
		if (LPConstants.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return false;
	}

	@Override
	public boolean hasRoute(int id, boolean flag, ItemIdentifier item) {
		if (LPConstants.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return false;
	}

	@Override
	public ExitRoute getExitFor(int id, boolean flag, ItemIdentifier item) {
		if (LPConstants.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return null;
	}

	@Override
	public ArrayList<List<ExitRoute>> getRouteTable() {
		if (LPConstants.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new ArrayList<List<ExitRoute>>();
	}

	@Override
	public List<ExitRoute> getIRoutersByCost() {
		if (LPConstants.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new LinkedList<ExitRoute>();
	}

	@Override
	public CoreRoutedPipe getPipe() {
		World worldObj = MainProxy.proxy.getWorld();
		if (worldObj == null) {
			return null;
		}
		TileEntity tile = worldObj.getTileEntity(_xCoord, _yCoord, _zCoord);

		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}
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
	public LPPosition getLPPosition() {
		return new LPPosition(_xCoord, _yCoord, _zCoord);
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
		//Not On Client Side
	}

	@Override
	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider() {
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
	public boolean isSideDisconneceted(ForgeDirection dir) {
		return false;
	}

	@Override
	public void updateInterests() {

	}

	@Override
	public List<ExitRoute> getDistanceTo(IRouter r) {
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
	public boolean isSubPoweredExit(ForgeDirection connection) {
		return false;
	}

	@Override
	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider() {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder("ServerRouter: {UUID: ");
		string.append(getId());
		string.append(", AT: (");
		string.append(_xCoord);
		string.append(", ");
		string.append(_yCoord);
		string.append(", ");
		string.append(_zCoord);
		return string.append(")").toString();
	}

	@Override
	public List<ExitRoute> getRoutersOnSide(ForgeDirection exitOrientation) {
		return null;
	}

	@Override
	public int getDimension() {
		return 0;
	}

	@Override
	public int getDistanceToNextPowerPipe(ForgeDirection dir) {
		return 0;
	}

	@Override
	public void queueTask(int i, IRouterQueuedTask callable) {}
}
