/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.LPConstants;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IRoutingDebugAdapter;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.pathfinder.PathFinder;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.OneList;
import logisticspipes.utils.StackTraceUtil;
import logisticspipes.utils.StackTraceUtil.Info;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ServerRouter implements IRouter, Comparable<ServerRouter> {

	// things with specific interests -- providers (including crafters)
	static HashMap<ItemIdentifier, Set<IRouter>> _globalSpecificInterests = new HashMap<ItemIdentifier, Set<IRouter>>();
	// things potentially interested in every item (chassi with generic sinks)
	static Set<IRouter> _genericInterests = new TreeSet<IRouter>();

	// things this pipe is interested in (either providing or sinking)
	Set<ItemIdentifier> _hasInterestIn = new TreeSet<ItemIdentifier>();
	boolean _hasGenericInterest;

	static final int REFRESH_TIME = 20;
	static int iterated = 0;// used pseudp-random to spread items over the tick range
	int ticksUntillNextInventoryCheck = 0;

	@Override
	public int hashCode() {
		return simpleID; // guaranteed to be unique, and uniform distribution over a range.
	}

	protected class LSA {

		public HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
		public List<Pair<ILogisticsPowerProvider, List<IFilter>>> power;
		public ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
	}

	private abstract class RouterRunnable implements Comparable<RouterRunnable>, Runnable {

		public abstract int getPrority();

		public abstract int localCompare(RouterRunnable o);

		@Override
		public int compareTo(RouterRunnable o) {
			if (o.getPrority() == getPrority()) {
				return localCompare(o);
			}
			return o.getPrority() - getPrority();
		}
	}

	private class UpdateRouterRunnable extends RouterRunnable {

		int newVersion = 0;
		boolean run = false;
		IRouter target;

		UpdateRouterRunnable(IRouter target) {
			run = true;
			newVersion = _LSAVersion;
			this.target = target;
		}

		@Override
		public void run() {
			if (!run) {
				return;
			}
			try {
				CoreRoutedPipe p = target.getCachedPipe();
				if (p == null) {
					run = false;
					return;
				}
				//spinlock during the first tick, we can't touch the routing table, untill Update() has been called on every pipe.
				for (int i = 0; i < 10 && p.stillNeedReplace(); i++) {
					Thread.sleep(10);
				}
				if (p.stillNeedReplace()) {
					return; // drop the pipe update if it still needs replace after 5 ticks.
				}
				CreateRouteTable(newVersion);
			} catch (Exception e) {
				e.printStackTrace();
			}
			run = false;
		}

		@Override
		public int getPrority() {
			return 0;
		}

		@Override
		public int localCompare(RouterRunnable o) {
			int c = 0;
			if (((UpdateRouterRunnable) o).newVersion <= 0) {
				c = newVersion - ((UpdateRouterRunnable) o).newVersion; // negative numbers have priority, more negative first
			}
			if (c != 0) {
				return 0;
			}
			c = target.getSimpleID() - ((UpdateRouterRunnable) o).target.getSimpleID(); // do things in order of router id, to minimize router recursion
			if (c != 0) {
				return 0;
			}
			c = ((UpdateRouterRunnable) o).newVersion - newVersion; // higher version first
			return c;
		}
	}

	private static int maxLSAUpdateIndex = 0;

	private class LSARouterRunnable extends RouterRunnable {

		private final int index = ServerRouter.maxLSAUpdateIndex++;
		HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
		ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> power;
		ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;

		LSARouterRunnable(HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric, ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> power, ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower) {
			this.neighboursWithMetric = neighboursWithMetric;
			this.power = power;
			this.subSystemPower = subSystemPower;
		}

		@Override
		public void run() {
			lockAndUpdateLSA(neighboursWithMetric, power, subSystemPower);
		}

		@Override
		public int getPrority() {
			return 1;
		}

		@Override
		public int localCompare(RouterRunnable o) {
			return index - ((LSARouterRunnable) o).index;
		}
	}

	// these are maps, not hashMaps because they are unmodifiable Collections to avoid concurrentModification exceptions.
	public Map<CoreRoutedPipe, ExitRoute> _adjacent = new HashMap<CoreRoutedPipe, ExitRoute>();
	public Map<IRouter, ExitRoute> _adjacentRouter = new HashMap<IRouter, ExitRoute>();
	public Map<IRouter, ExitRoute> _adjacentRouter_Old = new HashMap<IRouter, ExitRoute>();
	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> _powerAdjacent = new ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>>();
	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> _subSystemPowerAdjacent = new ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>>();

	public boolean[] sideDisconnected = new boolean[6];

	protected static int[] _lastLSAVersion = new int[0];
	protected int _LSAVersion = 0;
	protected final LSA _myLsa;

	protected UpdateRouterRunnable updateThread = null;

	protected static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
	protected static final Lock SharedLSADatabasereadLock = ServerRouter.SharedLSADatabaseLock.readLock();
	protected static final Lock SharedLSADatabasewriteLock = ServerRouter.SharedLSADatabaseLock.writeLock();
	protected final ReentrantReadWriteLock routingTableUpdateLock = new ReentrantReadWriteLock();
	protected final Lock routingTableUpdateReadLock = routingTableUpdateLock.readLock();
	protected final Lock routingTableUpdateWriteLock = routingTableUpdateLock.writeLock();
	public Object _externalRoutersByCostLock = new Object();

	protected static LSA[] SharedLSADatabase = new LSA[0];

	/** Map of router -> orientation for all known destinations **/
	public List<List<ExitRoute>> _routeTable = Collections.unmodifiableList(new ArrayList<List<ExitRoute>>());
	public List<ExitRoute> _routeCosts = Collections.unmodifiableList(new ArrayList<ExitRoute>());
	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> _LPPowerTable = Collections.unmodifiableList(new ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>>());
	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> _SubSystemPowerTable = Collections.unmodifiableList(new ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>>());

	private EnumSet<ForgeDirection> _routedExits = EnumSet.noneOf(ForgeDirection.class);
	private EnumMap<ForgeDirection, Integer> _subPowerExits = new EnumMap<ForgeDirection, Integer>(ForgeDirection.class);

	private static int firstFreeId = 1;
	private static BitSet simpleIdUsedSet = new BitSet();

	protected final int simpleID;
	public final UUID id;
	private int _dimension;
	@Getter
	private final int _xCoord;
	@Getter
	private final int _yCoord;
	@Getter
	private final int _zCoord;

	@Getter
	@Setter(value = AccessLevel.PRIVATE)
	private boolean destroied = false;

	private WeakReference<CoreRoutedPipe> _myPipeCache = null;
	private LinkedList<Pair<Integer, IRouterQueuedTask>> queue = new LinkedList<Pair<Integer, IRouterQueuedTask>>();

	@Override
	public void clearPipeCache() {
		_myPipeCache = null;
	}

	// called on server shutdown only
	public static void cleanup() {
		ServerRouter._globalSpecificInterests.clear();
		ServerRouter._genericInterests.clear();
		ServerRouter.SharedLSADatabasewriteLock.lock();
		ServerRouter.SharedLSADatabase = new LSA[0];
		ServerRouter._lastLSAVersion = new int[0];
		ServerRouter.SharedLSADatabasewriteLock.unlock();
		ServerRouter.simpleIdUsedSet.clear();
		ServerRouter.firstFreeId = 1;
	}

	private static int claimSimpleID() {
		int idx = ServerRouter.simpleIdUsedSet.nextClearBit(ServerRouter.firstFreeId);
		ServerRouter.firstFreeId = idx + 1;
		ServerRouter.simpleIdUsedSet.set(idx);
		return idx;
	}

	private static void releaseSimpleID(int idx) {
		ServerRouter.simpleIdUsedSet.clear(idx);
		if (idx < ServerRouter.firstFreeId) {
			ServerRouter.firstFreeId = idx;
		}
	}

	public static int getBiggestSimpleID() {
		return ServerRouter.simpleIdUsedSet.size();
	}

	public ServerRouter(UUID globalID, int dimension, int xCoord, int yCoord, int zCoord) {
		if (globalID != null) {
			id = globalID;
		} else {
			id = UUID.randomUUID();
		}
		_dimension = dimension;
		_xCoord = xCoord;
		_yCoord = yCoord;
		_zCoord = zCoord;
		clearPipeCache();
		_myLsa = new LSA();
		_myLsa.neighboursWithMetric = new HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>>();
		_myLsa.power = new ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>>();
		ServerRouter.SharedLSADatabasewriteLock.lock(); // any time after we claim the SimpleID, the database could be accessed at that index
		simpleID = ServerRouter.claimSimpleID();
		if (ServerRouter.SharedLSADatabase.length <= simpleID) {
			int newlength = ((int) (simpleID * 1.5)) + 1;
			LSA[] new_SharedLSADatabase = new LSA[newlength];
			System.arraycopy(ServerRouter.SharedLSADatabase, 0, new_SharedLSADatabase, 0, ServerRouter.SharedLSADatabase.length);
			ServerRouter.SharedLSADatabase = new_SharedLSADatabase;
			int[] new_lastLSAVersion = new int[newlength];
			System.arraycopy(ServerRouter._lastLSAVersion, 0, new_lastLSAVersion, 0, ServerRouter._lastLSAVersion.length);
			ServerRouter._lastLSAVersion = new_lastLSAVersion;
		}
		ServerRouter._lastLSAVersion[simpleID] = 0;
		ServerRouter.SharedLSADatabase[simpleID] = _myLsa; // make non-structural change (threadsafe)
		ServerRouter.SharedLSADatabasewriteLock.unlock();
	}

	@Override
	public int getSimpleID() {
		return simpleID;
	}

	@Override
	public boolean isInDim(int dimension) {
		return _dimension == dimension;
	}

	@Override
	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord) {
		return _dimension == dimension && _xCoord == xCoord && _yCoord == yCoord && _zCoord == zCoord;
	}

	@Override
	public LPPosition getLPPosition() {
		return new LPPosition(_xCoord, _yCoord, _zCoord);
	}

	@Override
	public CoreRoutedPipe getPipe() {
		CoreRoutedPipe crp = getCachedPipe();
		if (crp != null) {
			return crp;
		}
		World worldObj = DimensionManager.getWorld(_dimension);
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
		_myPipeCache = new WeakReference<CoreRoutedPipe>((CoreRoutedPipe) pipe.pipe);

		return (CoreRoutedPipe) pipe.pipe;
	}

	@Override
	public CoreRoutedPipe getCachedPipe() {
		if (_myPipeCache != null) {
			return _myPipeCache.get();
		}
		return null;
	}

	@Override
	public boolean isValidCache() {
		return getPipe() != null;
	}

	private void ensureRouteTableIsUpToDate(boolean force) {
		if (force && connectionNeedsChecking != 0) {
			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				updateLsa();
			}
		}
		if (_LSAVersion > ServerRouter._lastLSAVersion[simpleID]) {
			if (Configs.MULTI_THREAD_NUMBER > 0 && !force) {
				RoutingTableUpdateThread.add(new UpdateRouterRunnable(this));
			} else {
				CreateRouteTable(_LSAVersion);
			}
		}
	}

	@Override
	public List<List<ExitRoute>> getRouteTable() {
		ensureRouteTableIsUpToDate(true);
		return _routeTable;
	}

	@Override
	public List<ExitRoute> getIRoutersByCost() {
		ensureRouteTableIsUpToDate(true);
		return _routeCosts;
	}

	@Override
	public UUID getId() {
		return id;
	}

	private int connectionNeedsChecking = 0;
	private List<LPPosition> causedBy = new LinkedList<LPPosition>();

	private ITileEntityChangeListener localChangeListener = new ITileEntityChangeListener() {

		@Override
		public void pipeRemoved(LPPosition pos) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LPConstants.DEBUG) {
				causedBy.add(pos);
			}
		}

		@Override
		public void pipeAdded(LPPosition pos, ForgeDirection side) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LPConstants.DEBUG) {
				causedBy.add(pos);
			}
		}

		@Override
		public void pipeModified(LPPosition pos) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LPConstants.DEBUG) {
				causedBy.add(pos);
			}
		}
	};

	private Set<List<ITileEntityChangeListener>> listenedPipes = new HashSet<List<ITileEntityChangeListener>>();
	private Set<LPTileEntityObject> oldTouchedPipes = new HashSet<LPTileEntityObject>();

	/**
	 * Rechecks the piped connection to all adjacent routers as well as discover
	 * new ones.
	 */
	private boolean recheckAdjacent() {
		connectionNeedsChecking = 0;
		if (LPConstants.DEBUG) {
			causedBy.clear();
		}
		if (getPipe() != null) {
			if (getPipe().getDebug() != null && getPipe().getDebug().debugThisPipe) {
				Info info = StackTraceUtil.addTraceInformation("(" + getPipe().getX() + ", " + getPipe().getY() + ", " + getPipe().getZ() + ")");
				StackTraceUtil.printTrace();
				info.end();
			}
			getPipe().spawnParticle(Particles.LightRedParticle, 5);
		}

		LPTickHandler.adjChecksDone++;
		boolean adjacentChanged = false;
		CoreRoutedPipe thisPipe = getPipe();
		if (thisPipe == null) {
			return false;
		}
		HashMap<CoreRoutedPipe, ExitRoute> adjacent;
		List<Pair<ILogisticsPowerProvider, List<IFilter>>> power;
		List<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
		PathFinder finder = new PathFinder(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH, localChangeListener);
		power = finder.powerNodes;
		subSystemPower = finder.subPowerProvider;
		adjacent = finder.result;

		Map<ForgeDirection, List<CoreRoutedPipe>> pipeDirections = new HashMap<ForgeDirection, List<CoreRoutedPipe>>();

		for (Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
			List<CoreRoutedPipe> list = pipeDirections.get(entry.getValue().exitOrientation);
			if (list == null) {
				list = new ArrayList<CoreRoutedPipe>();
				pipeDirections.put(entry.getValue().exitOrientation, list);
			}
			list.add(entry.getKey());
		}

		for (Entry<ForgeDirection, List<CoreRoutedPipe>> entry : pipeDirections.entrySet()) {
			if (entry.getValue().size() > Configs.MAX_UNROUTED_CONNECTIONS) {
				for (CoreRoutedPipe pipe : entry.getValue()) {
					adjacent.remove(pipe);
				}
			}
		}

		for (List<ITileEntityChangeListener> list : listenedPipes) {
			if (!finder.listenedPipes.contains(list)) {
				list.remove(localChangeListener);
			}
		}
		listenedPipes = finder.listenedPipes;

		for (CoreRoutedPipe pipe : adjacent.keySet()) {
			if (pipe.stillNeedReplace()) {
				return false;
			}
		}

		boolean[] oldSideDisconnected = sideDisconnected;
		sideDisconnected = new boolean[6];
		checkSecurity(adjacent);

		boolean changed = false;

		for (int i = 0; i < 6; i++) {
			changed |= sideDisconnected[i] != oldSideDisconnected[i];
		}
		if (changed) {
			CoreRoutedPipe pipe = getPipe();
			if (pipe != null) {
				pipe.getWorld().notifyBlocksOfNeighborChange(pipe.getX(), pipe.getY(), pipe.getZ(), pipe.getWorld().getBlock(pipe.getX(), pipe.getY(), pipe.getZ()));
				pipe.refreshConnectionAndRender(false);
			}
			adjacentChanged = true;
		}

		if (_adjacent.size() != adjacent.size()) {
			adjacentChanged = true;
		}

		for (CoreRoutedPipe pipe : _adjacent.keySet()) {
			if (!adjacent.containsKey(pipe)) {
				adjacentChanged = true;
			}
		}
		if (_powerAdjacent != null) {
			if (power == null) {
				adjacentChanged = true;
			} else {
				for (Pair<ILogisticsPowerProvider, List<IFilter>> provider : _powerAdjacent) {
					if (!power.contains(provider)) {
						adjacentChanged = true;
					}
				}
			}
		}
		if (power != null) {
			if (_powerAdjacent == null) {
				adjacentChanged = true;
			} else {
				for (Pair<ILogisticsPowerProvider, List<IFilter>> provider : power) {
					if (!_powerAdjacent.contains(provider)) {
						adjacentChanged = true;
					}
				}
			}
		}
		if (_subSystemPowerAdjacent != null) {
			if (subSystemPower == null) {
				adjacentChanged = true;
			} else {
				for (Pair<ISubSystemPowerProvider, List<IFilter>> provider : _subSystemPowerAdjacent) {
					if (!subSystemPower.contains(provider)) {
						adjacentChanged = true;
					}
				}
			}
		}
		if (subSystemPower != null) {
			if (_subSystemPowerAdjacent == null) {
				adjacentChanged = true;
			} else {
				for (Pair<ISubSystemPowerProvider, List<IFilter>> provider : subSystemPower) {
					if (!_subSystemPowerAdjacent.contains(provider)) {
						adjacentChanged = true;
					}
				}
			}
		}
		for (Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
			ExitRoute oldExit = _adjacent.get(pipe.getKey());
			if (oldExit == null) {
				adjacentChanged = true;
				break;
			}
			ExitRoute newExit = pipe.getValue();

			if (!newExit.equals(oldExit)) {
				adjacentChanged = true;
				break;
			}
		}

		if (!oldTouchedPipes.equals(finder.touchedPipes)) {
			CacheHolder.clearCache(oldTouchedPipes);
			CacheHolder.clearCache(finder.touchedPipes);
			oldTouchedPipes = finder.touchedPipes;
			BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
			visited.set(getSimpleID());
			act(visited, new floodClearCache());
		}

		if (adjacentChanged) {
			HashMap<IRouter, ExitRoute> adjacentRouter = new HashMap<IRouter, ExitRoute>();
			EnumSet<ForgeDirection> routedexits = EnumSet.noneOf(ForgeDirection.class);
			EnumMap<ForgeDirection, Integer> subpowerexits = new EnumMap<ForgeDirection, Integer>(ForgeDirection.class);
			for (Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
				adjacentRouter.put(pipe.getKey().getRouter(), pipe.getValue());
				if ((pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) || pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom) && !routedexits.contains(pipe.getValue().exitOrientation))) {
					routedexits.add(pipe.getValue().exitOrientation);
				}
				if (!subpowerexits.containsKey(pipe.getValue().exitOrientation) && pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
					subpowerexits.put(pipe.getValue().exitOrientation, PathFinder.messureDistanceToNextRoutedPipe(getLPPosition(), pipe.getValue().exitOrientation, pipe.getKey().getWorld()));
				}
			}
			_adjacent = Collections.unmodifiableMap(adjacent);
			_adjacentRouter_Old = _adjacentRouter;
			_adjacentRouter = Collections.unmodifiableMap(adjacentRouter);
			if (power != null) {
				_powerAdjacent = Collections.unmodifiableList(power);
			} else {
				_powerAdjacent = null;
			}
			if (subSystemPower != null) {
				_subSystemPowerAdjacent = Collections.unmodifiableList(subSystemPower);
			} else {
				_subSystemPowerAdjacent = null;
			}
			_routedExits = routedexits;
			_subPowerExits = subpowerexits;
			SendNewLSA();
		}
		return adjacentChanged;
	}

	private void checkSecurity(HashMap<CoreRoutedPipe, ExitRoute> adjacent) {
		CoreRoutedPipe pipe = getPipe();
		if (pipe == null) {
			return;
		}
		UUID id = pipe.getSecurityID();
		List<CoreRoutedPipe> toRemove = new ArrayList<CoreRoutedPipe>();
		if (id != null) {
			for (Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
				if (!entry.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) && !entry.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom)) {
					continue;
				}
				UUID thatId = entry.getKey().getSecurityID();
				if (!(pipe instanceof PipeItemsFirewall)) {
					if (thatId == null) {
						entry.getKey().insetSecurityID(id);
					} else if (!id.equals(thatId)) {
						sideDisconnected[entry.getValue().exitOrientation.ordinal()] = true;
					}
				} else {
					if (!(entry.getKey() instanceof PipeItemsFirewall)) {
						if (thatId != null && !id.equals(thatId)) {
							sideDisconnected[entry.getValue().exitOrientation.ordinal()] = true;
						}
					}
				}
			}
			for (Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
				if (sideDisconnected[entry.getValue().exitOrientation.ordinal()]) {
					toRemove.add(entry.getKey());
				}
			}
			for (CoreRoutedPipe remove : toRemove) {
				adjacent.remove(remove);
			}
		}
	}

	private void SendNewLSA() {
		HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric = new HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>>();
		for (Entry<IRouter, ExitRoute> adjacent : _adjacentRouter.entrySet()) {
			neighboursWithMetric.put(adjacent.getKey(), new Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>(adjacent.getValue().distanceToDestination, adjacent.getValue().connectionDetails, adjacent.getValue().filters, adjacent.getValue().blockDistance));
		}
		ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> power = null;
		if (_powerAdjacent != null) {
			power = new ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>>();
			for (Pair<ILogisticsPowerProvider, List<IFilter>> provider : _powerAdjacent) {
				power.add(provider);
			}
		}
		ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower = null;
		if (_subSystemPowerAdjacent != null) {
			subSystemPower = new ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>>();
			for (Pair<ISubSystemPowerProvider, List<IFilter>> provider : _subSystemPowerAdjacent) {
				subSystemPower.add(provider);
			}
		}
		if (Configs.MULTI_THREAD_NUMBER > 0) {
			RoutingTableUpdateThread.add(new LSARouterRunnable(neighboursWithMetric, power, subSystemPower));
		} else {
			lockAndUpdateLSA(neighboursWithMetric, power, subSystemPower);
		}
	}

	private void lockAndUpdateLSA(HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric, ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> power, ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower) {
		ServerRouter.SharedLSADatabasewriteLock.lock();
		_myLsa.neighboursWithMetric = neighboursWithMetric;
		_myLsa.power = power;
		_myLsa.subSystemPower = subSystemPower;
		ServerRouter.SharedLSADatabasewriteLock.unlock();
	}

	public void CreateRouteTable(int version_to_update_to) {
		CreateRouteTable(version_to_update_to, new DummyRoutingDebugAdapter());
	}

	/**
	 * Create a route table from the link state database
	 */
	public void CreateRouteTable(int version_to_update_to, IRoutingDebugAdapter debug) {

		if (ServerRouter._lastLSAVersion[simpleID] >= version_to_update_to && !debug.independent()) {
			return; // this update is already done.
		}

		//Dijkstra!

		debug.init();

		int routingTableSize = ServerRouter.getBiggestSimpleID();
		if (routingTableSize == 0) {
			routingTableSize = ServerRouter.SharedLSADatabase.length; // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
		}

		/**
		 * same info as above, but sorted by distance -- sorting is implicit,
		 * because Dijkstra finds the closest routes first.
		 **/
		List<ExitRoute> routeCosts = new ArrayList<ExitRoute>(routingTableSize);

		//Add the current Router
		routeCosts.add(new ExitRoute(this, this, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, 0, EnumSet.allOf(PipeRoutingConnectionType.class), 0));

		ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> powerTable;
		if (_powerAdjacent != null) {
			powerTable = new ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>>(_powerAdjacent);
		} else {
			powerTable = new ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>>(5);
		}
		ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
		if (_subSystemPowerAdjacent != null) {
			subSystemPower = new ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>>(_subSystemPowerAdjacent);
		} else {
			subSystemPower = new ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>>(5);
		}

		//space and time inefficient, a bitset with 3 bits per node would save a lot but makes the main iteration look like a complete mess
		ArrayList<EnumSet<PipeRoutingConnectionType>> closedSet = new ArrayList<EnumSet<PipeRoutingConnectionType>>(ServerRouter.getBiggestSimpleID());
		for (int i = 0; i < ServerRouter.getBiggestSimpleID(); i++) {
			closedSet.add(null);
		}

		ArrayList<EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>> filterList = new ArrayList<EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>>(ServerRouter.getBiggestSimpleID());
		for (int i = 0; i < ServerRouter.getBiggestSimpleID(); i++) {
			filterList.add(null);
		}

		/** The total cost for the candidate route **/
		PriorityQueue<ExitRoute> candidatesCost = new PriorityQueue<ExitRoute>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.

		//Init candidates
		// the shortest way to go to an adjacent item is the adjacent item.
		for (Entry<IRouter, ExitRoute> pipe : _adjacentRouter.entrySet()) {
			ExitRoute currentE = pipe.getValue();
			IRouter newRouter = pipe.getKey();
			if (newRouter != null) {
				ExitRoute newER = new ExitRoute(newRouter, newRouter, currentE.distanceToDestination, currentE.connectionDetails, currentE.filters, new ArrayList<IFilter>(0), currentE.blockDistance);
				candidatesCost.add(newER);
				debug.newCanidate(newER);
			}
		}

		debug.start(candidatesCost, closedSet, filterList);

		ServerRouter.SharedLSADatabasereadLock.lock(); // readlock, not inside the while - too costly to aquire, then release.
		ExitRoute lowestCostNode;
		while ((lowestCostNode = candidatesCost.poll()) != null) {
			if (!lowestCostNode.hasActivePipe()) {
				continue;
			}

			if (debug.isDebug()) {
				ServerRouter.SharedLSADatabasereadLock.unlock();
			}
			debug.nextPipe(lowestCostNode);
			if (debug.isDebug()) {
				ServerRouter.SharedLSADatabasereadLock.lock();
			}

			for (ExitRoute e : candidatesCost) {
				e.debug.isNewlyAddedCanidate = false;
			}

			//if the node does not have any flags not in the closed set, check it
			EnumSet<PipeRoutingConnectionType> lowestCostClosedFlags = closedSet.get(lowestCostNode.destination.getSimpleID());
			if (lowestCostClosedFlags == null) {
				lowestCostClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
			}
			if (lowestCostClosedFlags.containsAll(lowestCostNode.getFlagsNoCopy())) {
				continue;
			}

			if (debug.isDebug()) {
				EnumSet<PipeRoutingConnectionType> newFlags = lowestCostNode.getFlags();
				newFlags.removeAll(lowestCostClosedFlags);

				debug.newFlagsForPipe(newFlags);
			}

			EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> filters = filterList.get(lowestCostNode.destination.getSimpleID());

			debug.filterList(filters);

			if (filters != null) {
				boolean containsNewInfo = false;
				for (PipeRoutingConnectionType type : lowestCostNode.getFlagsNoCopy()) {
					if (lowestCostClosedFlags.contains(type)) {
						continue;
					}
					if (!filters.containsKey(type)) {
						containsNewInfo = true;
						break;
					}
					boolean matches = false;
					List<List<IFilter>> list = filters.get(type);
					for (List<IFilter> filter : list) {
						if (lowestCostNode.filters.containsAll(filter)) {
							matches = true;
							break;
						}
					}
					if (!matches) {
						containsNewInfo = true;
						break;
					}
				}
				if (!containsNewInfo) {
					continue;
				}
			}

			//Add new candidates from the newly approved route
			LSA lsa = null;
			if (lowestCostNode.destination.getSimpleID() < ServerRouter.SharedLSADatabase.length) {
				lsa = ServerRouter.SharedLSADatabase[lowestCostNode.destination.getSimpleID()];
			}
			if (lsa == null) {
				lowestCostNode.removeFlags(lowestCostClosedFlags);
				lowestCostClosedFlags.addAll(lowestCostNode.getFlagsNoCopy());
				if (lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom)) {
					routeCosts.add(lowestCostNode);
				}
				closedSet.set(lowestCostNode.destination.getSimpleID(), lowestCostClosedFlags);
				continue;
			}
			if (lowestCostNode.containsFlag(PipeRoutingConnectionType.canPowerFrom)) {
				if (lsa.power != null && (lsa.power.isEmpty() == false)) {
					for (Pair<ILogisticsPowerProvider, List<IFilter>> p : lsa.power) {
						Pair<ILogisticsPowerProvider, List<IFilter>> entry = p.copy();
						List<IFilter> list = new ArrayList<IFilter>();
						list.addAll(p.getValue2());
						list.addAll(lowestCostNode.filters);
						entry.setValue2(Collections.unmodifiableList(list));
						if (!powerTable.contains(entry)) {
							powerTable.add(entry);
						}
					}
				}
			}
			if (lowestCostNode.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
				if (lsa.subSystemPower != null && (lsa.subSystemPower.isEmpty() == false)) {
					for (Pair<ISubSystemPowerProvider, List<IFilter>> p : lsa.subSystemPower) {
						Pair<ISubSystemPowerProvider, List<IFilter>> entry = p.copy();
						List<IFilter> list = new ArrayList<IFilter>();
						list.addAll(p.getValue2());
						list.addAll(lowestCostNode.filters);
						entry.setValue2(Collections.unmodifiableList(list));
						if (!subSystemPower.contains(entry)) {
							subSystemPower.add(entry);
						}
					}
				}
			}
			Iterator<Entry<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>>> it = lsa.neighboursWithMetric.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> newCandidate = it.next();
				double candidateCost = lowestCostNode.distanceToDestination + newCandidate.getValue().getValue1();
				int blockDistance = lowestCostNode.blockDistance + newCandidate.getValue().getValue4();
				EnumSet<PipeRoutingConnectionType> newCT = lowestCostNode.getFlags();
				newCT.retainAll(newCandidate.getValue().getValue2());
				if (!newCT.isEmpty()) {
					ExitRoute next = new ExitRoute(lowestCostNode.root, newCandidate.getKey(), candidateCost, newCT, lowestCostNode.filters, newCandidate.getValue().getValue3(), blockDistance);
					next.debug.isTraced = lowestCostNode.debug.isTraced;
					candidatesCost.add(next);
					debug.newCanidate(next);
				}
			}

			lowestCostClosedFlags = lowestCostClosedFlags.clone();

			lowestCostNode.removeFlags(lowestCostClosedFlags);
			lowestCostClosedFlags.addAll(lowestCostNode.getFlagsNoCopy());
			if (lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
				routeCosts.add(lowestCostNode);
			}
			EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> map = filterList.get(lowestCostNode.destination.getSimpleID());
			if (map == null) {
				map = new EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>(PipeRoutingConnectionType.class);
				filterList.set(lowestCostNode.destination.getSimpleID(), map);
			}
			for (PipeRoutingConnectionType type : lowestCostNode.getFlagsNoCopy()) {
				if (!map.containsKey(type)) {
					map.put(type, new ArrayList<List<IFilter>>());
				}
				map.get(type).add(Collections.unmodifiableList(new ArrayList<IFilter>(lowestCostNode.filters)));
			}
			if (lowestCostNode.filters.isEmpty()) {
				closedSet.set(lowestCostNode.destination.getSimpleID(), lowestCostClosedFlags);
			}

			if (debug.isDebug()) {
				ServerRouter.SharedLSADatabasereadLock.unlock();
			}
			debug.handledPipe();
			if (debug.isDebug()) {
				ServerRouter.SharedLSADatabasereadLock.lock();
			}
		}
		ServerRouter.SharedLSADatabasereadLock.unlock();

		debug.stepOneDone();

		//Build route table
		ArrayList<List<ExitRoute>> routeTable = new ArrayList<List<ExitRoute>>(ServerRouter.getBiggestSimpleID() + 1);
		while (simpleID >= routeTable.size()) {
			routeTable.add(null);
		}
		routeTable.set(simpleID, new OneList<ExitRoute>(new ExitRoute(this, this, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, 0, EnumSet.allOf(PipeRoutingConnectionType.class), 0)));

		Iterator<ExitRoute> itr = routeCosts.iterator();
		while (itr.hasNext()) {
			ExitRoute node = itr.next();
			IRouter firstHop = node.root;
			ExitRoute hop = _adjacentRouter.get(firstHop);
			if (hop == null) {
				continue;
			}
			node.root = this; // replace the root with this, rather than the first hop.
			node.exitOrientation = hop.exitOrientation;
			while (node.destination.getSimpleID() >= routeTable.size()) { // the array will not expand, as it is init'd to contain enough elements
				routeTable.add(null);
			}

			List<ExitRoute> current = routeTable.get(node.destination.getSimpleID());
			if (current != null && !current.isEmpty()) {
				List<ExitRoute> list = new ArrayList<ExitRoute>();
				list.addAll(current);
				list.add(node);
				routeTable.set(node.destination.getSimpleID(), Collections.unmodifiableList(list));
			} else {
				routeTable.set(node.destination.getSimpleID(), new OneList<ExitRoute>(node));
			}
		}
		debug.stepTwoDone();
		if (!debug.independent()) {
			routingTableUpdateWriteLock.lock();
			if (version_to_update_to == _LSAVersion) {
				ServerRouter.SharedLSADatabasereadLock.lock();

				if (ServerRouter._lastLSAVersion[simpleID] < version_to_update_to) {
					ServerRouter._lastLSAVersion[simpleID] = version_to_update_to;
					_LPPowerTable = Collections.unmodifiableList(powerTable);
					_SubSystemPowerTable = Collections.unmodifiableList(subSystemPower);
					_routeTable = Collections.unmodifiableList(routeTable);
					_routeCosts = Collections.unmodifiableList(routeCosts);
				}
				ServerRouter.SharedLSADatabasereadLock.unlock();
			}
			routingTableUpdateWriteLock.unlock();
		}
		if (getCachedPipe() != null) {
			getCachedPipe().spawnParticle(Particles.LightGreenParticle, 5);
		}

		debug.done();
	}

	@Override
	public void act(BitSet hasBeenProcessed, IRAction actor) {
		if (hasBeenProcessed.get(simpleID)) {
			return;
		}
		hasBeenProcessed.set(simpleID);
		if (!actor.isInteresting(this)) {
			return;
		}

		actor.doTo(this);
		for (IRouter r : _adjacentRouter.keySet()) {
			r.act(hasBeenProcessed, actor);
		}
		return;
	}

	/**
	 * Flags the last sent LSA as expired. Each router will be responsible of
	 * purging it from its database.
	 */
	@Override
	public void destroy() {
		ServerRouter.SharedLSADatabasewriteLock.lock(); // take a write lock so that we don't overlap with any ongoing route updates
		if (simpleID < ServerRouter.SharedLSADatabase.length) {
			ServerRouter.SharedLSADatabase[simpleID] = null;
		}
		ServerRouter.SharedLSADatabasewriteLock.unlock();
		removeAllInterests();

		clearPipeCache();
		setDestroied(true);
		SimpleServiceLocator.routerManager.removeRouter(simpleID);
		for (List<ITileEntityChangeListener> list : listenedPipes) {
			list.remove(localChangeListener);
		}
		updateAdjacentAndLsa();
		ServerRouter.releaseSimpleID(simpleID);
	}

	private void removeAllInterests() {
		removeGenericInterest();
		for (ItemIdentifier i : _hasInterestIn) {
			removeInterest(i);
		}
		_hasInterestIn.clear();
	}

	/**
	 * Floodfill recheckAdjacent, leave _prevAdjacentRouter around for LSA
	 * updating
	 */
	class floodCheckAdjacent implements IRAction {

		@Override
		public boolean isInteresting(IRouter that) {
			return that.checkAdjacentUpdate();
		}

		@Override
		public void doTo(IRouter that) {

		}
	}

	@Override
	public boolean checkAdjacentUpdate() {
		boolean blockNeedsUpdate = recheckAdjacent();
		if (!blockNeedsUpdate) {
			return false;
		}

		CoreRoutedPipe pipe = getPipe();
		if (pipe == null) {
			return true;
		}
		pipe.refreshRender(true);
		return true;
	}

	/**
	 * Floodfill LSA increment and clean up the _prevAdjacentRouter list left by
	 * floodCheckAdjacent
	 */
	class flagForLSAUpdate implements IRAction {

		@Override
		public boolean isInteresting(IRouter that) {
			return true;
		}

		@Override
		public void doTo(IRouter that) {
			that.flagForRoutingUpdate();
		}
	}

	class floodClearCache implements IRAction {

		@Override
		public boolean isInteresting(IRouter that) {
			return true;
		}

		@Override
		public void doTo(IRouter that) {
			CacheHolder.clearCache(((ServerRouter) that).oldTouchedPipes);
		}
	}

	@Override
	public void flagForRoutingUpdate() {
		_LSAVersion++;
		//if(LogisticsPipes.DEBUG)
		//System.out.println("[LogisticsPipes] flag for routing update to "+_LSAVersion+" for Node" +  simpleID);
	}

	private void updateAdjacentAndLsa() {
		//this already got a checkAdjacentUpdate, so start the recursion with neighbors
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		IRAction flood = new floodCheckAdjacent();
		visited.set(simpleID);
		// for all connected updatecurrent and previous
		for (IRouter r : _adjacentRouter_Old.keySet()) {
			r.act(visited, flood);
		}
		for (IRouter r : _adjacentRouter.keySet()) {
			r.act(visited, flood);
		}
		updateLsa();
	}

	private void updateLsa() {
		//now increment LSA version in the network
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		for (IRouter r : _adjacentRouter_Old.keySet()) {
			r.act(visited, new flagForLSAUpdate());
		}
		_adjacentRouter_Old = new HashMap<IRouter, ExitRoute>();
		act(visited, new flagForLSAUpdate());
	}

	@Override
	public void update(boolean doFullRefresh, CoreRoutedPipe pipe) {
		if (connectionNeedsChecking == 2) {
			Info info = null;
			if (LPConstants.DEBUG) {
				info = StackTraceUtil.addTraceInformation(causedBy.toString());
			}
			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				updateLsa();
			}
			if (LPConstants.DEBUG) {
				info.end();
			}
		}
		if (connectionNeedsChecking == 1) {
			connectionNeedsChecking = 2;
		}
		handleQueuedTasks(pipe);
		updateInterests();
		if (doFullRefresh) {
			if (pipe.container instanceof ILPTEInformation && ((ILPTEInformation) pipe.container).getObject() != null) {
				if (!((ILPTEInformation) pipe.container).getObject().changeListeners.contains(localChangeListener)) {
					((ILPTEInformation) pipe.container).getObject().changeListeners.add(localChangeListener);
				}
			}

			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				//updateAdjacentAndLsa();
				updateLsa();
			}
			ensureRouteTableIsUpToDate(false);
			return;
		}
		if (Configs.MULTI_THREAD_NUMBER > 0) {
			ensureRouteTableIsUpToDate(false);
		}
	}

	private void handleQueuedTasks(CoreRoutedPipe pipe) {
		while (!queue.isEmpty()) {
			Pair<Integer, IRouterQueuedTask> element = queue.poll();
			if (element.getValue1() > MainProxy.getGlobalTick()) {
				element.getValue2().call(pipe, this);
			}
		}
	}

	/************* IROUTER *******************/

	@Override
	public boolean isRoutedExit(ForgeDirection o) {
		return _routedExits.contains(o);
	}

	@Override
	public boolean isSubPoweredExit(ForgeDirection o) {
		return _subPowerExits.containsKey(o);
	}

	@Override
	public int getDistanceToNextPowerPipe(ForgeDirection dir) {
		return _subPowerExits.get(dir);
	}

	@Override
	public ExitRoute getExitFor(int id, boolean active, ItemIdentifier type) {
		ensureRouteTableIsUpToDate(true);
		if (getRouteTable().size() <= id || getRouteTable().get(id) == null) {
			return null;
		}
		outer:
			for (ExitRoute exit : getRouteTable().get(id)) {
				if (exit.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
					for (IFilter filter : exit.filters) {
						if (!active) {
							if (filter.blockRouting() || filter.isBlocked() == filter.isFilteredItem(type)) {
								continue outer;
							}
						} else {
							if ((filter.blockProvider() && filter.blockCrafting()) || filter.isBlocked() == filter.isFilteredItem(type)) {
								continue outer;
							}
						}
					}
					return exit;
				}
			}
		return null;
	}

	@Override
	public boolean hasRoute(int id, boolean active, ItemIdentifier type) {
		if (!SimpleServiceLocator.routerManager.isRouterUnsafe(id, false)) {
			return false;
		}
		ensureRouteTableIsUpToDate(true);
		if (getRouteTable().size() <= id) {
			return false;
		}
		List<ExitRoute> source = getRouteTable().get(id);
		if (source == null) {
			return false;
		}
		outer:
			for (ExitRoute exit : source) {
				if (exit.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
					for (IFilter filter : exit.filters) {
						if (!active) {
							if (filter.blockRouting() || filter.isBlocked() == filter.isFilteredItem(type)) {
								continue outer;
							}
						} else {
							if ((filter.blockProvider() && filter.blockCrafting()) || filter.isBlocked() == filter.isFilteredItem(type)) {
								continue outer;
							}
						}
					}
					return true;
				}
			}
		return false;
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
	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider() {
		return _LPPowerTable;
	}

	@Override
	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider() {
		return _SubSystemPowerTable;
	}

	@Override
	public boolean isSideDisconneceted(ForgeDirection dir) {
		return ForgeDirection.UNKNOWN != dir && sideDisconnected[dir.ordinal()];
	}

	@Override
	public void updateInterests() {
		if (--ticksUntillNextInventoryCheck > 0) {
			return;
		}
		ticksUntillNextInventoryCheck = ServerRouter.REFRESH_TIME;
		if (ServerRouter.iterated++ % simpleID == 0) {
			ticksUntillNextInventoryCheck++; // randomly wait 1 extra tick - just so that every router doesn't tick at the same time
		}
		if (ServerRouter.iterated >= ServerRouter.getBiggestSimpleID()) {
			ServerRouter.iterated = 0;
		}
		CoreRoutedPipe pipe = getPipe();
		if (pipe == null) {
			return;
		}
		if (pipe.hasGenericInterests()) {
			declareGenericInterest();
		} else {
			removeGenericInterest();
		}
		Set<ItemIdentifier> newInterests = pipe.getSpecificInterests();
		if (newInterests == null) {
			newInterests = new TreeSet<ItemIdentifier>();
		}
		if (!newInterests.equals(_hasInterestIn)) {
			for (ItemIdentifier i : _hasInterestIn) {
				if (!newInterests.contains(i)) {
					removeInterest(i);
				}
			}
			for (ItemIdentifier i : newInterests) {
				if (!_hasInterestIn.contains(i)) {
					addInterest(i);
				}
			}
			_hasInterestIn = newInterests;
		}
	}

	private void removeGenericInterest() {
		_hasGenericInterest = false;
		ServerRouter._genericInterests.remove(this);
	}

	private void declareGenericInterest() {
		_hasGenericInterest = true;
		ServerRouter._genericInterests.add(this);
	}

	private void addInterest(ItemIdentifier items) {
		Set<IRouter> interests = ServerRouter._globalSpecificInterests.get(items);
		if (interests == null) {
			interests = new TreeSet<IRouter>();
			ServerRouter._globalSpecificInterests.put(items, interests);
		}
		interests.add(this);
	}

	private void removeInterest(ItemIdentifier p2) {
		Set<IRouter> interests = ServerRouter._globalSpecificInterests.get(p2);
		if (interests == null) {
			return;
		}
		interests.remove(this);
		if (interests.isEmpty()) {
			ServerRouter._globalSpecificInterests.remove(p2);
		}

	}

	public boolean hasGenericInterest() {
		return _hasGenericInterest;
	}

	public boolean hasInterestIn(ItemIdentifier item) {
		return _hasInterestIn.contains(item);
	}

	public static BitSet getRoutersInterestedIn(ItemIdentifier item) {
		BitSet s = new BitSet(ServerRouter.getBiggestSimpleID() + 1);
		if (ServerRouter._genericInterests != null) {
			for (IRouter r : ServerRouter._genericInterests) {
				s.set(r.getSimpleID());
			}
		}
		if (item == null) {
			return s;
		}
		Set<IRouter> specifics = ServerRouter._globalSpecificInterests.get(item);
		if (specifics != null) {
			for (IRouter r : specifics) {
				s.set(r.getSimpleID());
			}
		}
		specifics = ServerRouter._globalSpecificInterests.get(item.getUndamaged());
		if (specifics != null) {
			for (IRouter r : specifics) {
				s.set(r.getSimpleID());
			}
		}
		specifics = ServerRouter._globalSpecificInterests.get(item.getIgnoringNBT());
		if (specifics != null) {
			for (IRouter r : specifics) {
				s.set(r.getSimpleID());
			}
		}
		specifics = ServerRouter._globalSpecificInterests.get(item.getUndamaged().getIgnoringNBT());
		if (specifics != null) {
			for (IRouter r : specifics) {
				s.set(r.getSimpleID());
			}
		}
		specifics = ServerRouter._globalSpecificInterests.get(item.getIgnoringData());
		if (specifics != null) {
			for (IRouter r : specifics) {
				s.set(r.getSimpleID());
			}
		}
		specifics = ServerRouter._globalSpecificInterests.get(item.getIgnoringData().getIgnoringNBT());
		if (specifics != null) {
			for (IRouter r : specifics) {
				s.set(r.getSimpleID());
			}
		}
		return s;
	}

	public static BitSet getRoutersInterestedIn(IResource item) {
		if (item instanceof ItemResource) {
			return ServerRouter.getRoutersInterestedIn(((ItemResource) item).getItem());
		} else if (item instanceof FluidResource) {
			return ServerRouter.getRoutersInterestedIn(((FluidResource) item).getFluid().getItemIdentifier());
		} else if (item instanceof DictResource) {
			DictResource dict = (DictResource) item;
			BitSet s = new BitSet(ServerRouter.getBiggestSimpleID() + 1);
			if (ServerRouter._genericInterests != null) {
				for (IRouter r : ServerRouter._genericInterests) {
					s.set(r.getSimpleID());
				}
			}
			for (Entry<ItemIdentifier, Set<IRouter>> entry : ServerRouter._globalSpecificInterests.entrySet()) {
				if (dict.matches(entry.getKey(), IResource.MatchSettings.NORMAL)) {
					for (IRouter r : entry.getValue()) {
						s.set(r.getSimpleID());
					}
				}
			}
			return s;
		}
		return new BitSet(ServerRouter.getBiggestSimpleID() + 1);
	}

	@Override
	public int compareTo(ServerRouter o) {
		return simpleID - o.simpleID;
	}

	@Override
	public List<ExitRoute> getDistanceTo(IRouter r) {
		ensureRouteTableIsUpToDate(true);
		int id = r.getSimpleID();
		if (_routeTable.size() <= id) {
			return new ArrayList<ExitRoute>(0);
		}
		List<ExitRoute> result = _routeTable.get(id);
		return result != null ? result : new ArrayList<ExitRoute>(0);
	}

	public static Map<ItemIdentifier, Set<IRouter>> getInterestedInSpecifics() {
		return ServerRouter._globalSpecificInterests;
	}

	public static Set<IRouter> getInterestedInGeneral() {
		return ServerRouter._genericInterests;
	}

	@Override
	public void clearInterests() {
		removeAllInterests();
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder("ServerRouter: {ID: ");
		string.append(simpleID);
		string.append(", UUID: ");
		string.append(getId());
		string.append(", AT: (");
		string.append(_dimension);
		string.append(", ");
		string.append(_xCoord);
		string.append(", ");
		string.append(_yCoord);
		string.append(", ");
		string.append(_zCoord);
		string.append("), Version: ");
		string.append(_LSAVersion);
		string.append("), Destroied: ");
		string.append(isDestroied());
		return string.append("}").toString();
	}

	@Override
	public void forceLsaUpdate() {
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		act(visited, new flagForLSAUpdate());
	}

	@Override
	public List<ExitRoute> getRoutersOnSide(ForgeDirection direction) {
		List<ExitRoute> routers = new ArrayList<ExitRoute>();
		for (ExitRoute exit : _adjacentRouter.values()) {
			if (exit.exitOrientation == direction) {
				routers.add(exit);
			}
		}
		return routers;
	}

	@Override
	public int getDimension() {
		return _dimension;
	}

	@Override
	public void queueTask(int i, IRouterQueuedTask callable) {
		queue.add(new Pair<Integer, IRouterQueuedTask>(i + MainProxy.getGlobalTick(), callable));
	}
}
