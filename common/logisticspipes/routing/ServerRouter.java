/**
 * Copyright (c) Krapht, 2011
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IRoutingDebugAdapter;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.LogisticsModule;
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
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class ServerRouter implements IRouter, Comparable<ServerRouter> {

	public static final int REFRESH_TIME = 20;

	protected static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
	protected static final Lock SharedLSADatabasereadLock = ServerRouter.SharedLSADatabaseLock.readLock();
	protected static final Lock SharedLSADatabasewriteLock = ServerRouter.SharedLSADatabaseLock.writeLock();
	protected static int[] _lastLSAVersion = new int[0];
	protected static LSA[] SharedLSADatabase = new LSA[0];

	// things with specific interests -- providers (including crafters)
	@Nonnull
	private static final ConcurrentHashMap<ItemIdentifier, TreeSet<ServerRouter>> globalSpecificInterests = new ConcurrentHashMap<>();

	// things potentially interested in every item (chassi with generic sinks)
	@Nonnull
	private static TreeSet<ServerRouter> genericInterests = new TreeSet<>();
	private static final Lock genericInterestsWLock = new ReentrantLock();

	// things this pipe is interested in (either providing or sinking)
	@Nonnull
	private TreeSet<ItemIdentifier> interests = new TreeSet<>();
	private final Lock interestsRWLock = new ReentrantLock();

	static int iterated = 0;// used pseudp-random to spread items over the tick range
	private static int maxLSAUpdateIndex = 0;
	private static int firstFreeId = 1;
	private static final BitSet simpleIdUsedSet = new BitSet();

	public final UUID id;
	protected final LSA _myLsa;
	protected final ReentrantReadWriteLock routingTableUpdateLock = new ReentrantReadWriteLock();
	protected final Lock routingTableUpdateWriteLock = routingTableUpdateLock.writeLock();
	protected final int simpleID;
	@Getter
	private final int _xCoord;
	@Getter
	private final int _yCoord;
	@Getter
	private final int _zCoord;
	// these are maps, not hashMaps because they are unmodifiable Collections to avoid concurrentModification exceptions.
	public Map<CoreRoutedPipe, ExitRoute> _adjacent = new HashMap<>();
	public Map<ServerRouter, ExitRoute> _adjacentRouter = new HashMap<>();
	public Map<ServerRouter, ExitRoute> _adjacentRouter_Old = new HashMap<>();
	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> _powerAdjacent = new ArrayList<>();
	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> _subSystemPowerAdjacent = new ArrayList<>();
	public boolean[] sideDisconnected = new boolean[6];
	/**
	 * Map of router -> orientation for all known destinations
	 **/
	public List<List<ExitRoute>> _routeTable = Collections.unmodifiableList(new ArrayList<>());
	public List<ExitRoute> _routeCosts = Collections.unmodifiableList(new ArrayList<>());
	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> _LPPowerTable = Collections.unmodifiableList(new ArrayList<>());
	public List<Pair<ISubSystemPowerProvider, List<IFilter>>> _SubSystemPowerTable = Collections.unmodifiableList(new ArrayList<>());
	protected int _LSAVersion = 0;
	int ticksUntillNextInventoryCheck = 0;
	private EnumSet<EnumFacing> _routedExits = EnumSet.noneOf(EnumFacing.class);
	private EnumMap<EnumFacing, Integer> _subPowerExits = new EnumMap<>(EnumFacing.class);
	private final int _dimension;
	private WeakReference<CoreRoutedPipe> _myPipeCache = null;
	private final LinkedList<Pair<Integer, IRouterQueuedTask>> queue = new LinkedList<>();
	int connectionNeedsChecking = 0;
	private final List<DoubleCoordinates> causedBy = new LinkedList<>();
	private boolean isDestroyed = false;
	private final ITileEntityChangeListener localChangeListener = new ITileEntityChangeListener() {

		@Override
		public void pipeRemoved(DoubleCoordinates pos) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LogisticsPipes.isDEBUG()) {
				causedBy.add(pos);
			}
		}

		@Override
		public void pipeAdded(DoubleCoordinates pos, EnumFacing side) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LogisticsPipes.isDEBUG()) {
				causedBy.add(pos);
			}
		}

		@Override
		public void pipeModified(DoubleCoordinates pos) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LogisticsPipes.isDEBUG()) {
				causedBy.add(pos);
			}
		}
	};
	private Set<List<ITileEntityChangeListener>> listenedPipes = new HashSet<>();
	private Set<LPTileEntityObject> oldTouchedPipes = new HashSet<>();

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
		_myLsa.neighboursWithMetric = new HashMap<>();
		_myLsa.power = new ArrayList<>();
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

	// called on server shutdown only
	public static void cleanup() {
		ServerRouter.globalSpecificInterests.clear();
		ServerRouter.genericInterests.clear();
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

	private static void setBitsForItemInterests(@Nonnull final BitSet bitset, @Nonnull final ItemIdentifier itemid) {
		TreeSet<ServerRouter> specifics = ServerRouter.globalSpecificInterests.get(itemid);
		if (specifics != null) {
			for (IRouter r : specifics) {
				bitset.set(r.getSimpleID());
			}
		}
	}

	public static BitSet getRoutersInterestedIn(ItemIdentifier item) {
		final BitSet s = new BitSet(ServerRouter.getBiggestSimpleID() + 1);
		for (IRouter r : ServerRouter.genericInterests) {
			s.set(r.getSimpleID());
		}
		if (item != null) {
			Stream.of(item, item.getUndamaged(), item.getIgnoringNBT(), item.getUndamaged().getIgnoringNBT(), item.getIgnoringDamage(), item.getIgnoringDamage().getIgnoringNBT())
					.forEach(itemid -> setBitsForItemInterests(s, itemid));
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
			for (IRouter r : ServerRouter.genericInterests) {
				s.set(r.getSimpleID());
			}
			ServerRouter.globalSpecificInterests.entrySet().stream()
					.filter(entry -> dict.matches(entry.getKey(), IResource.MatchSettings.NORMAL))
					.flatMap(entry -> entry.getValue().stream())
					.forEach(router -> s.set(router.simpleID));
			return s;
		}
		return new BitSet(ServerRouter.getBiggestSimpleID() + 1);
	}

	public static void forEachGlobalSpecificInterest(BiConsumer<ItemIdentifier, NavigableSet<ServerRouter>> consumer) {
		ServerRouter.globalSpecificInterests.forEach((itemIdentifier, serverRouters) -> consumer.accept(itemIdentifier, Collections.unmodifiableNavigableSet(serverRouters)));
	}

	public static NavigableSet<ServerRouter> getInterestedInGeneral() {
		return Collections.unmodifiableNavigableSet(ServerRouter.genericInterests);
	}

	@Override
	public int hashCode() {
		return simpleID; // guaranteed to be unique, and uniform distribution over a range.
	}

	@Override
	public void clearPipeCache() {
		_myPipeCache = null;
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
	public DoubleCoordinates getLPPosition() {
		return new DoubleCoordinates(_xCoord, _yCoord, _zCoord);
	}

	@Override
	public CoreRoutedPipe getPipe() {
		CoreRoutedPipe crp = getCachedPipe();
		if (crp != null) {
			return crp;
		}
		World world = DimensionManager.getWorld(_dimension);
		if (world == null) {
			return null;
		}
		TileEntity tile = world.getTileEntity(new BlockPos(_xCoord, _yCoord, _zCoord));

		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}
		LogisticsTileGenericPipe pipe = (LogisticsTileGenericPipe) tile;
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		_myPipeCache = new WeakReference<>((CoreRoutedPipe) pipe.pipe);

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
	public boolean isCacheInvalid() {
		return getPipe() == null;
	}

	private void lazyUpdateRoutingTable() {
		if (_LSAVersion > ServerRouter._lastLSAVersion[simpleID]) {
			if (Configs.MULTI_THREAD_NUMBER > 0) {
				RoutingTableUpdateThread.add(new UpdateRouterRunnable(this));
			} else {
				CreateRouteTable(_LSAVersion);
			}
		}
	}

	void ensureLatestRoutingTable() {
		if (connectionNeedsChecking != 0) {
			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				updateLsa();
			}
		}
		if (_LSAVersion > ServerRouter._lastLSAVersion[simpleID]) {
			CreateRouteTable(_LSAVersion);
		}
	}

	@Override
	public List<List<ExitRoute>> getRouteTable() {
		ensureLatestRoutingTable();
		return _routeTable;
	}

	@Override
	public List<ExitRoute> getIRoutersByCost() {
		ensureLatestRoutingTable();
		return _routeCosts;
	}

	@Override
	public UUID getId() {
		return id;
	}

	/**
	 * Rechecks the piped connection to all adjacent routers as well as discover
	 * new ones.
	 */
	private boolean recheckAdjacent() {
		connectionNeedsChecking = 0;
		if (LogisticsPipes.isDEBUG()) {
			causedBy.clear();
		}
		if (getPipe() != null) {
			/*
			if (getPipe().getDebug() != null && getPipe().getDebug().debugThisPipe) {
				Info info = StackTraceUtil.addTraceInformation("(" + getPipe().getX() + ", " + getPipe().getY() + ", " + getPipe().getZ() + ")");
				StackTraceUtil.printTrace();
				info.end();
			}
			*/
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

		Map<EnumFacing, List<CoreRoutedPipe>> pipeDirections = new HashMap<>();

		for (Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
			List<CoreRoutedPipe> list = pipeDirections.computeIfAbsent(entry.getValue().exitOrientation, k -> new ArrayList<>());
			list.add(entry.getKey());
		}

		pipeDirections.entrySet().stream()
				.filter(entry -> entry.getValue().size() > Configs.MAX_UNROUTED_CONNECTIONS)
				.forEach(entry -> entry.getValue().forEach(adjacent::remove));

		listenedPipes.stream().filter(list -> !finder.listenedPipes.contains(list)).forEach(list -> list.remove(localChangeListener));
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
				pipe.getWorld().markAndNotifyBlock(pipe.getPos(), pipe.getWorld().getChunkFromBlockCoords(pipe.getPos()), pipe.getWorld().getBlockState(pipe.getPos()), pipe.getWorld().getBlockState(pipe.getPos()), 3);
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
				break;
			}
		}
		if (_powerAdjacent != null) {
			if (power == null) {
				adjacentChanged = true;
			} else {
				for (Pair<ILogisticsPowerProvider, List<IFilter>> provider : _powerAdjacent) {
					if (!power.contains(provider)) {
						adjacentChanged = true;
						break;
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
						break;
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
						break;
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
						break;
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
			HashMap<ServerRouter, ExitRoute> adjacentRouter = new HashMap<>();
			EnumSet<EnumFacing> routedexits = EnumSet.noneOf(EnumFacing.class);
			EnumMap<EnumFacing, Integer> subpowerexits = new EnumMap<>(EnumFacing.class);
			for (Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
				adjacentRouter.put((ServerRouter) pipe.getKey().getRouter(), pipe.getValue());
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
			List<CoreRoutedPipe> toRemove = adjacent.entrySet().stream()
					.filter(entry -> sideDisconnected[entry.getValue().exitOrientation.ordinal()])
					.map(Entry::getKey)
					.collect(Collectors.toList());
			toRemove.forEach(adjacent::remove);
		}
	}

	private void SendNewLSA() {
		HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric = new HashMap<>();
		for (Entry<ServerRouter, ExitRoute> adjacent : _adjacentRouter.entrySet()) {
			neighboursWithMetric.put(adjacent.getKey(), new Quartet<>(adjacent
					.getValue().distanceToDestination, adjacent.getValue().connectionDetails, adjacent
					.getValue().filters, adjacent.getValue().blockDistance));
		}
		ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> power = null;
		if (_powerAdjacent != null) {
			power = new ArrayList<>(_powerAdjacent);
		}
		ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower = null;
		if (_subSystemPowerAdjacent != null) {
			subSystemPower = new ArrayList<>(_subSystemPowerAdjacent);
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
		List<ExitRoute> routeCosts = new ArrayList<>(routingTableSize);

		//Add the current Router
		routeCosts.add(new ExitRoute(this, this, null, null, 0, EnumSet.allOf(PipeRoutingConnectionType.class), 0));

		ArrayList<Pair<ILogisticsPowerProvider, List<IFilter>>> powerTable;
		if (_powerAdjacent != null) {
			powerTable = new ArrayList<>(_powerAdjacent);
		} else {
			powerTable = new ArrayList<>(5);
		}
		ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
		if (_subSystemPowerAdjacent != null) {
			subSystemPower = new ArrayList<>(_subSystemPowerAdjacent);
		} else {
			subSystemPower = new ArrayList<>(5);
		}

		//space and time inefficient, a bitset with 3 bits per node would save a lot but makes the main iteration look like a complete mess
		ArrayList<EnumSet<PipeRoutingConnectionType>> closedSet = new ArrayList<>(ServerRouter.getBiggestSimpleID());
		for (int i = 0; i < ServerRouter.getBiggestSimpleID(); i++) {
			closedSet.add(null);
		}

		ArrayList<EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>> filterList = new ArrayList<>(ServerRouter
				.getBiggestSimpleID());
		for (int i = 0; i < ServerRouter.getBiggestSimpleID(); i++) {
			filterList.add(null);
		}

		/** The total cost for the candidate route **/
		PriorityQueue<ExitRoute> candidatesCost = new PriorityQueue<>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.

		//Init candidates
		// the shortest way to go to an adjacent item is the adjacent item.
		for (Entry<ServerRouter, ExitRoute> pipe : _adjacentRouter.entrySet()) {
			ExitRoute currentE = pipe.getValue();
			IRouter newRouter = pipe.getKey();
			if (newRouter != null) {
				ExitRoute newER = new ExitRoute(newRouter, newRouter, currentE.distanceToDestination, currentE.connectionDetails, currentE.filters, new ArrayList<>(0), currentE.blockDistance);
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
				if (lsa.power != null && !lsa.power.isEmpty()) {
					for (Pair<ILogisticsPowerProvider, List<IFilter>> p : lsa.power) {
						Pair<ILogisticsPowerProvider, List<IFilter>> entry = p.copy();
						List<IFilter> list = new ArrayList<>();
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
				if (lsa.subSystemPower != null && !lsa.subSystemPower.isEmpty()) {
					for (Pair<ISubSystemPowerProvider, List<IFilter>> p : lsa.subSystemPower) {
						Pair<ISubSystemPowerProvider, List<IFilter>> entry = p.copy();
						List<IFilter> list = new ArrayList<>();
						list.addAll(p.getValue2());
						list.addAll(lowestCostNode.filters);
						entry.setValue2(Collections.unmodifiableList(list));
						if (!subSystemPower.contains(entry)) {
							subSystemPower.add(entry);
						}
					}
				}
			}
			for (Entry<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> newCandidate : lsa.neighboursWithMetric.entrySet()) {
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
				map = new EnumMap<>(PipeRoutingConnectionType.class);
				filterList.set(lowestCostNode.destination.getSimpleID(), map);
			}
			for (PipeRoutingConnectionType type : lowestCostNode.getFlagsNoCopy()) {
				if (!map.containsKey(type)) {
					map.put(type, new ArrayList<>());
				}
				map.get(type).add(Collections.unmodifiableList(new ArrayList<>(lowestCostNode.filters)));
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
		ArrayList<List<ExitRoute>> routeTable = new ArrayList<>(ServerRouter.getBiggestSimpleID() + 1);
		while (simpleID >= routeTable.size()) {
			routeTable.add(null);
		}
		routeTable.set(simpleID, new OneList<>(new ExitRoute(this, this, null, null, 0, EnumSet
				.allOf(PipeRoutingConnectionType.class), 0)));

		for (ExitRoute node : routeCosts) {
			IRouter firstHop = node.root;
			ExitRoute hop = _adjacentRouter.get(firstHop);
			if (hop == null) {
				continue;
			}
			node.root = this; // replace the root with this, rather than the first hop.
			node.exitOrientation = hop.exitOrientation;
			while (node.destination.getSimpleID() >= routeTable
					.size()) { // the array will not expand, as it is init'd to contain enough elements
				routeTable.add(null);
			}

			List<ExitRoute> current = routeTable.get(node.destination.getSimpleID());
			if (current != null && !current.isEmpty()) {
				List<ExitRoute> list = new ArrayList<>(current);
				list.add(node);
				routeTable.set(node.destination.getSimpleID(), Collections.unmodifiableList(list));
			} else {
				routeTable.set(node.destination.getSimpleID(), new OneList<>(node));
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

	/**
	 * @param hasBeenProcessed a BitSet flagging which nodes have already been acted on. The router should set the bit for its own id.
	 */
	public void act(BitSet hasBeenProcessed, Action actor) {
		if (hasBeenProcessed.get(simpleID)) {
			return;
		}
		hasBeenProcessed.set(simpleID);
		if (!actor.isInteresting(this)) {
			return;
		}

		actor.doTo(this);
		for (ServerRouter r : _adjacentRouter.keySet()) {
			r.act(hasBeenProcessed, actor);
		}
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
		isDestroyed = true;
		SimpleServiceLocator.routerManager.removeRouter(simpleID);
		for (List<ITileEntityChangeListener> list : listenedPipes) {
			list.remove(localChangeListener);
		}
		updateAdjacentAndLsa();
		ServerRouter.releaseSimpleID(simpleID);
	}

	private void removeAllInterests() {
		removeGenericInterest();

		interestsRWLock.lock();
		try {
			interests.forEach(this::removeGlobalInterest);
			interests.clear();
		} finally {
			interestsRWLock.unlock();
		}
	}

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

	public void flagForRoutingUpdate() {
		_LSAVersion++;
		//if(LogisticsPipes.DEBUG)
		//System.out.println("[LogisticsPipes] targeted for routing update to "+_LSAVersion+" for Node" +  simpleID);
	}

	private void updateAdjacentAndLsa() {
		//this already got a checkAdjacentUpdate, so start the recursion with neighbors
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		Action flood = new floodCheckAdjacent();
		visited.set(simpleID);
		// for all connected updatecurrent and previous
		for (ServerRouter r : _adjacentRouter_Old.keySet()) {
			r.act(visited, flood);
		}
		for (ServerRouter r : _adjacentRouter.keySet()) {
			r.act(visited, flood);
		}
		updateLsa();
	}

	void updateLsa() {
		//now increment LSA version in the network
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		for (ServerRouter r : _adjacentRouter_Old.keySet()) {
			r.act(visited, new flagForLSAUpdate());
		}
		_adjacentRouter_Old = new HashMap<>();
		act(visited, new flagForLSAUpdate());
	}

	@Override
	public void update(boolean doFullRefresh, CoreRoutedPipe pipe) {
		if (connectionNeedsChecking == 2) {
			ensureChangeListenerAttachedToPipe(pipe);

			final Info info = StackTraceUtil.addTraceInformation(causedBy::toString);
			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				updateLsa();
			}
			info.end();

			ensureChangeListenerAttachedToPipe(pipe);
		}
		if (connectionNeedsChecking == 1) {
			connectionNeedsChecking = 2;
		}
		handleQueuedTasks(pipe);
		updateInterests();
		if (doFullRefresh) {
			ensureChangeListenerAttachedToPipe(pipe);

			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				//updateAdjacentAndLsa();
				updateLsa();
			}

			ensureChangeListenerAttachedToPipe(pipe);
			lazyUpdateRoutingTable();
		} else if (Configs.MULTI_THREAD_NUMBER > 0) {
			lazyUpdateRoutingTable();
		}
	}

	private void ensureChangeListenerAttachedToPipe(CoreRoutedPipe pipe) {
		if (pipe.container instanceof ILPTEInformation && ((ILPTEInformation) pipe.container).getObject() != null) {
			if (!((ILPTEInformation) pipe.container).getObject().changeListeners.contains(localChangeListener)) {
				((ILPTEInformation) pipe.container).getObject().changeListeners.add(localChangeListener);
			}
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
	public boolean isRoutedExit(EnumFacing o) {
		return _routedExits.contains(o);
	}

	@Override
	public boolean isSubPoweredExit(EnumFacing o) {
		return _subPowerExits.containsKey(o);
	}

	@Override
	public int getDistanceToNextPowerPipe(EnumFacing dir) {
		return _subPowerExits.get(dir);
	}

	@Override
	public ExitRoute getExitFor(int id, boolean active, ItemIdentifier type) {
		ensureLatestRoutingTable();
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
		ensureLatestRoutingTable();
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
	public boolean isSideDisconnected(EnumFacing dir) {
		return null != dir && sideDisconnected[dir.ordinal()];
	}

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
		TreeSet<ItemIdentifier> newInterests = new TreeSet<>();
		pipe.collectSpecificInterests(newInterests);

		interestsRWLock.lock();
		try {
			if (newInterests.size() == interests.size() && newInterests.containsAll(interests)) {
				// interests are up-to-date
				return;
			}

			interests.stream().filter(itemid -> !newInterests.contains(itemid)).forEach(this::removeGlobalInterest);
			newInterests.stream().filter(itemid -> !interests.contains(itemid)).forEach(this::addGlobalInterest);
			interests = newInterests;
		} finally {
			interestsRWLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void removeGenericInterest() {
		genericInterestsWLock.lock();
		try {
			final TreeSet<ServerRouter> newGenericInterests = (TreeSet<ServerRouter>) ServerRouter.genericInterests.clone();
			if (newGenericInterests.remove(this)) {
				ServerRouter.genericInterests = newGenericInterests;
			}
		} finally {
			genericInterestsWLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void declareGenericInterest() {
		genericInterestsWLock.lock();
		try {
			final TreeSet<ServerRouter> newGenericInterests = (TreeSet<ServerRouter>) ServerRouter.genericInterests.clone();
			if (newGenericInterests.add(this)) {
				ServerRouter.genericInterests = newGenericInterests;
			}
		} finally {
			genericInterestsWLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void addGlobalInterest(ItemIdentifier itemid) {
		ServerRouter.globalSpecificInterests.compute(itemid, (unused, serverRouters) -> {
			final TreeSet<ServerRouter> newServerRouters = serverRouters == null ? new TreeSet<>() : (TreeSet<ServerRouter>) serverRouters.clone();
			newServerRouters.add(this);
			return newServerRouters;
		});
	}

	@SuppressWarnings("unchecked")
	private void removeGlobalInterest(ItemIdentifier itemid) {
		ServerRouter.globalSpecificInterests.computeIfPresent(itemid, (unused, serverRouters) -> {
			if (serverRouters.equals(ObjectSets.singleton(this))) {
				return null;
			} else {
				final TreeSet<ServerRouter> newServerRouters = (TreeSet<ServerRouter>) serverRouters.clone();
				newServerRouters.remove(this);
				return newServerRouters;
			}
		});
	}

	@Override
	public int compareTo(ServerRouter o) {
		return simpleID - o.simpleID;
	}

	@Override
	public List<ExitRoute> getDistanceTo(IRouter r) {
		ensureLatestRoutingTable();
		int id = r.getSimpleID();
		if (_routeTable.size() <= id) {
			return new ArrayList<>(0);
		}
		List<ExitRoute> result = _routeTable.get(id);
		return result != null ? result : new ArrayList<>(0);
	}

	@Override
	public void clearInterests() {
		removeAllInterests();
	}

	@Override
	public String toString() {
		return String.format("ServerRouter: {ID: %d, UUID: %s, AT: (%d, %d, %d, %d), Version: %d), Destroyed: %s}", simpleID, getId(), _dimension, _xCoord, _yCoord, _zCoord, _LSAVersion, isDestroyed);
	}

	@Override
	public void forceLsaUpdate() {
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		act(visited, new flagForLSAUpdate());
	}

	@Override
	public List<ExitRoute> getRoutersOnSide(EnumFacing direction) {
		return _adjacentRouter.values().stream()
				.filter(exit -> exit.exitOrientation == direction)
				.collect(Collectors.toList());
	}

	@Override
	public void queueTask(int i, IRouterQueuedTask callable) {
		queue.add(new Pair<>(i + MainProxy.getGlobalTick(), callable));
	}

	protected static class LSA {

		public HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
		public List<Pair<ILogisticsPowerProvider, List<IFilter>>> power;
		public ArrayList<Pair<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
	}

	private abstract static class RouterRunnable implements Comparable<RouterRunnable>, Runnable {

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

	interface Action {
		boolean isInteresting(ServerRouter router);

		void doTo(ServerRouter router);
	}

	/**
	 * Floodfill recheckAdjacent, leave _prevAdjacentRouter around for LSA
	 * updating
	 */
	static class floodCheckAdjacent implements Action {

		public boolean isInteresting(ServerRouter router) {
			return router.checkAdjacentUpdate();
		}

		public void doTo(ServerRouter router) {}
	}

	/**
	 * Floodfill LSA increment and clean up the _prevAdjacentRouter list left by
	 * floodCheckAdjacent
	 */
	static class flagForLSAUpdate implements Action {

		public boolean isInteresting(ServerRouter router) {
			return true;
		}

		public void doTo(ServerRouter router) {
			router.flagForRoutingUpdate();
		}
	}

	static class floodClearCache implements Action {

		public boolean isInteresting(ServerRouter router) {
			return true;
		}

		public void doTo(ServerRouter router) {
			CacheHolder.clearCache(router.oldTouchedPipes);
		}
	}

	private class UpdateRouterRunnable extends RouterRunnable {

		int newVersion;
		boolean run;
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
}
