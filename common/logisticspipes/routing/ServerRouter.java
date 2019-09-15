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
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import net.minecraftforge.common.DimensionManager;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import lombok.Getter;

import logisticspipes.LPConstants;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
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
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.request.resources.Resource;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.routing.pathfinder.PathFinder;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.StackTraceUtil;
import logisticspipes.utils.StackTraceUtil.Info;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Tuple2;
import logisticspipes.utils.tuples.Tuple4;
import network.rs485.logisticspipes.config.LPConfiguration;
import network.rs485.logisticspipes.routing.request.Resource;
import network.rs485.logisticspipes.util.ItemVariant;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public final class ServerRouter implements Router, Comparable<ServerRouter> {

	private static final ReentrantReadWriteLock SHARED_LSA_DATABASE_LOCK = new ReentrantReadWriteLock();
	private static final Lock SHARED_LSA_DATABASE_READ_LOCK = ServerRouter.SHARED_LSA_DATABASE_LOCK.readLock();
	private static final Lock SHARED_LSA_DATABASE_WRITE_LOCK = ServerRouter.SHARED_LSA_DATABASE_LOCK.writeLock();

	private static final int REFRESH_TIME = 20;

	private static int[] lastLsaVersion = new int[0];
	private static LSA[] sharedLsaDatabase = new LSA[0];

	// things with specific interests -- providers (including crafters)
	static HashMap<ItemVariant, Set<Router>> globalSpecificInterests = new HashMap<>();

	// things potentially interested in every item (chassi with generic sinks)
	static Set<Router> genericInterests = new HashSet<>();
	static int iterated = 0; // used pseudo-random to spread items over the tick range

	private static int maxLSAUpdateIndex = 0;
	private static int firstFreeId = 1;
	private static BitSet simpleIdUsedSet = new BitSet();
	public final UUID id;
	protected final LSA lsa;
	protected final ReentrantReadWriteLock routingTableUpdateLock = new ReentrantReadWriteLock();
	protected final Lock routingTableUpdateReadLock = routingTableUpdateLock.readLock();
	protected final Lock routingTableUpdateWriteLock = routingTableUpdateLock.writeLock();
	protected final int simpleID;

	// these are maps, not hashMaps because they are unmodifiable Collections to avoid concurrentModification exceptions.
	public Map<CoreRoutedPipe, ExitRoute> adjacent = Collections.emptyMap();
	public Map<Router, ExitRoute> adjacentRouter = Collections.emptyMap();
	public Map<Router, ExitRoute> prevAdjacentRouter = Collections.emptyMap();
	public List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> powerAdjacent = new ArrayList<>();
	public List<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPowerAdjacent = new ArrayList<>();
	public boolean[] sideDisconnected = new boolean[6];

	/**
	 * Map of router -> orientation for all known destinations
	 **/
	public List<List<ExitRoute>> _routeTable = Collections.unmodifiableList(new ArrayList<>());
	public List<ExitRoute> _routeCosts = Collections.unmodifiableList(new ArrayList<>());
	public List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> _LPPowerTable = Collections.unmodifiableList(new ArrayList<>());
	public List<Tuple2<ISubSystemPowerProvider, List<IFilter>>> _SubSystemPowerTable = Collections.unmodifiableList(new ArrayList<>());
	protected int lsaVersion = 0;
	protected UpdateRouterRunnable updateThread = null;

	// things this pipe is interested in (either providing or sinking)
	private Set<ItemVariant> hasInterestIn = new TreeSet<>();
	boolean hasGenericInterest;
	private int ticksUntilNextInventoryCheck = 0;
	private EnumSet<Direction> _routedExits = EnumSet.noneOf(Direction.class);
	private EnumMap<Direction, Integer> _subPowerExits = new EnumMap<>(Direction.class);
	private final World world;
	private final BlockPos pos;

	@Getter
	private boolean destroyed = false;

	private WeakReference<CoreRoutedPipe> _myPipeCache = null;
	private LinkedList<Tuple2<Integer, RouterQueuedTask>> queue = new LinkedList<>();
	private int connectionNeedsChecking = 0;
	private List<BlockPos> causedBy = new LinkedList<>();

	private ITileEntityChangeListener localChangeListener = new ITileEntityChangeListener() {

		@Override
		public void pipeRemoved(BlockPos pos) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LPConstants.DEBUG) {
				causedBy.add(pos);
			}
		}

		@Override
		public void pipeAdded(BlockPos pos, Direction side) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LPConstants.DEBUG) {
				causedBy.add(pos);
			}
		}

		@Override
		public void pipeModified(BlockPos pos) {
			if (connectionNeedsChecking == 0) {
				connectionNeedsChecking = 1;
			}
			if (LPConstants.DEBUG) {
				causedBy.add(pos);
			}
		}
	};

	private Set<List<ITileEntityChangeListener>> listenedPipes = new HashSet<>();
	private Set<LPTileEntityObject> oldTouchedPipes = new HashSet<>();

	public ServerRouter(UUID globalID, World world, BlockPos pos) {
		if (globalID != null) {
			id = globalID;
		} else {
			id = UUID.randomUUID();
		}

		this.world = world;
		this.pos = pos;

		clearPipeCache();
		lsa = new LSA();
		lsa.neighboursWithMetric = new HashMap<>();
		lsa.power = new ArrayList<>();
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.lock(); // any time after we claim the SimpleID, the database could be accessed at that index
		simpleID = ServerRouter.claimSimpleID();
		if (ServerRouter.sharedLsaDatabase.length <= simpleID) {
			int newLength = ((int) (simpleID * 1.5)) + 1;
			LSA[] new_SharedLSADatabase = new LSA[newLength];
			System.arraycopy(ServerRouter.sharedLsaDatabase, 0, new_SharedLSADatabase, 0, ServerRouter.sharedLsaDatabase.length);
			ServerRouter.sharedLsaDatabase = new_SharedLSADatabase;
			int[] new_lastLSAVersion = new int[newLength];
			System.arraycopy(ServerRouter.lastLsaVersion, 0, new_lastLSAVersion, 0, ServerRouter.lastLsaVersion.length);
			ServerRouter.lastLsaVersion = new_lastLSAVersion;
		}
		ServerRouter.lastLsaVersion[simpleID] = 0;
		ServerRouter.sharedLsaDatabase[simpleID] = lsa; // make non-structural change (threadsafe)
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.unlock();
	}

	// called on server shutdown only
	public static void cleanup() {
		ServerRouter.globalSpecificInterests.clear();
		ServerRouter.genericInterests.clear();
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.lock();
		ServerRouter.sharedLsaDatabase = new LSA[0];
		ServerRouter.lastLsaVersion = new int[0];
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.unlock();
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

	public static Set<UUID> getRoutersInterestedIn(ItemVariant item) {
		Set<UUID> routers = new HashSet<>();
		if (ServerRouter.genericInterests != null) {
			for (Router r : ServerRouter.genericInterests) {
				routers.add(r.getId());
			}
		}
		if (item == null) {
			return routers;
		}
		Set<Router> specifics = ServerRouter.globalSpecificInterests.get(item);
		if (specifics != null) {
			for (Router r : specifics) {
				routers.add(r.getId());
			}
		}
		specifics = ServerRouter.globalSpecificInterests.get(item.getUndamaged());
		if (specifics != null) {
			routers.addAll(specifics);
			for (Router r : specifics) {
				routers.add(r.getId());
			}
		}
		specifics = ServerRouter.globalSpecificInterests.get(item.getIgnoringNBT());
		if (specifics != null) {
			for (Router r : specifics) {
				routers.add(r.getId());
			}
		}
		specifics = ServerRouter.globalSpecificInterests.get(item.getUndamaged().getIgnoringNBT());
		if (specifics != null) {
			for (Router r : specifics) {
				routers.add(r.getId());
			}
		}
		specifics = ServerRouter.globalSpecificInterests.get(item.getIgnoringData());
		if (specifics != null) {
			for (Router r : specifics) {
				routers.add(r.getId());
			}
		}
		specifics = ServerRouter.globalSpecificInterests.get(item.getIgnoringData().getIgnoringNBT());
		if (specifics != null) {
			for (Router r : specifics) {
				routers.add(r.getId());
			}
		}
		return routers;
	}

	public static Set<UUID> getRoutersInterestedIn(FluidVolume fluid) {
		return Collections.emptySet();
	}

	public static Set<UUID> getRoutersInterestedIn(Resource resource) {
		if (resource instanceof Resource.Item) {
			return ServerRouter.getRoutersInterestedIn(((Resource.Item) resource).getStack());
		} else if (resource instanceof Resource.Fluid) {
			return ServerRouter.getRoutersInterestedIn(((Resource.Fluid) resource).getVolume());
		} else if (resource instanceof Resource.Dict) {
			Resource.Dict dict = (Resource.Dict) resource;
			Set<UUID> routers = new HashSet<>();
			if (ServerRouter.genericInterests != null) {
				for (Router r : ServerRouter.genericInterests) {
					routers.add(r.getId());
				}
			}
			ServerRouter.globalSpecificInterests.entrySet().stream()
					.filter(entry -> dict.matches(entry.getKey(), false)).forEach(entry -> {
				for (Router r : entry.getValue()) {
					routers.add(r.getId());
				}
			});
			return routers;
		}
		return Collections.emptySet();
	}

	public static Map<ItemVariant, Set<Router>> getInterestedInSpecifics() {
		return ServerRouter.globalSpecificInterests;
	}

	public static Set<Router> getInterestedInGeneral() {
		return ServerRouter.genericInterests;
	}

	@Override
	public void clearPipeCache() {
		_myPipeCache = null;
	}

	@Override
	public int getSimpleId() {
		return simpleID;
	}

	@Override
	public boolean isInDim(World world) {
		return this.world == world;
	}

	@Override
	public boolean isAt(World world, BlockPos pos) {
		return this.world == world && this.pos.equals(pos);
	}

	@Override
	public CoreRoutedPipe getPipe() {
		CoreRoutedPipe crp = getCachedPipe();
		if (crp != null) {
			return crp;
		}
		BlockEntity tile = world.getBlockEntity(new BlockPos(_xCoord, _yCoord, _zCoord));

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
		if (lsaVersion > ServerRouter.lastLsaVersion[simpleID]) {
			if (LPConfiguration.INSTANCE.getThreads() > 0 && !force) {
				RoutingTableUpdateThread.add(new UpdateRouterRunnable(this));
			} else {
				CreateRouteTable(lsaVersion);
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
		List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> power;
		List<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
		PathFinder finder = new PathFinder(thisPipe.container, LPConfiguration.INSTANCE.getPipeDetectionCount(), LPConfiguration.INSTANCE.getPipeDetectionLength(), localChangeListener);
		power = finder.powerNodes;
		subSystemPower = finder.subPowerProvider;
		adjacent = finder.result;

		Map<Direction, List<CoreRoutedPipe>> pipeDirections = new HashMap<>();

		for (Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
			List<CoreRoutedPipe> list = pipeDirections.computeIfAbsent(entry.getValue().exitOrientation, k -> new ArrayList<>());
			list.add(entry.getKey());
		}

		pipeDirections.entrySet().stream()
				.filter(entry -> entry.getValue().size() > LPConfiguration.INSTANCE.getMaxUnroutedConnections())
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

		if (this.adjacent.size() != adjacent.size()) {
			adjacentChanged = true;
		}

		for (CoreRoutedPipe pipe : this.adjacent.keySet()) {
			if (!adjacent.containsKey(pipe)) {
				adjacentChanged = true;
				break;
			}
		}
		if (powerAdjacent != null) {
			if (power == null) {
				adjacentChanged = true;
			} else {
				for (Tuple2<ILogisticsPowerProvider, List<IFilter>> provider : powerAdjacent) {
					if (!power.contains(provider)) {
						adjacentChanged = true;
						break;
					}
				}
			}
		}
		if (power != null) {
			if (powerAdjacent == null) {
				adjacentChanged = true;
			} else {
				for (Tuple2<ILogisticsPowerProvider, List<IFilter>> provider : power) {
					if (!powerAdjacent.contains(provider)) {
						adjacentChanged = true;
						break;
					}
				}
			}
		}
		if (subSystemPowerAdjacent != null) {
			if (subSystemPower == null) {
				adjacentChanged = true;
			} else {
				for (Tuple2<ISubSystemPowerProvider, List<IFilter>> provider : subSystemPowerAdjacent) {
					if (!subSystemPower.contains(provider)) {
						adjacentChanged = true;
						break;
					}
				}
			}
		}
		if (subSystemPower != null) {
			if (subSystemPowerAdjacent == null) {
				adjacentChanged = true;
			} else {
				for (Tuple2<ISubSystemPowerProvider, List<IFilter>> provider : subSystemPower) {
					if (!subSystemPowerAdjacent.contains(provider)) {
						adjacentChanged = true;
						break;
					}
				}
			}
		}
		for (Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
			ExitRoute oldExit = this.adjacent.get(pipe.getKey());
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
			visited.set(getSimpleId());
			act(visited, new FloodClearCache());
		}

		if (adjacentChanged) {
			HashMap<Router, ExitRoute> adjacentRouter = new HashMap<>();
			EnumSet<Direction> routedexits = EnumSet.noneOf(Direction.class);
			EnumMap<Direction, Integer> subpowerexits = new EnumMap<>(Direction.class);
			for (Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
				adjacentRouter.put(pipe.getKey().getRouter(), pipe.getValue());
				if ((pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) || pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom) && !routedexits.contains(pipe.getValue().exitOrientation))) {
					routedexits.add(pipe.getValue().exitOrientation);
				}
				if (!subpowerexits.containsKey(pipe.getValue().exitOrientation) && pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
					subpowerexits.put(pipe.getValue().exitOrientation, PathFinder.messureDistanceToNextRoutedPipe(getLPPosition(), pipe.getValue().exitOrientation, pipe.getKey().getWorld()));
				}
			}
			this.adjacent = Collections.unmodifiableMap(adjacent);
			prevAdjacentRouter = this.adjacentRouter;
			this.adjacentRouter = Collections.unmodifiableMap(adjacentRouter);
			if (power != null) {
				powerAdjacent = Collections.unmodifiableList(power);
			} else {
				powerAdjacent = null;
			}
			if (subSystemPower != null) {
				subSystemPowerAdjacent = Collections.unmodifiableList(subSystemPower);
			} else {
				subSystemPowerAdjacent = null;
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
		HashMap<Router, Tuple4<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric = new HashMap<>();
		for (Entry<Router, ExitRoute> adjacent : adjacentRouter.entrySet()) {
			neighboursWithMetric.put(adjacent.getKey(), new Tuple4<>(adjacent
					.getValue().distanceToDestination, adjacent.getValue().connectionDetails, adjacent
					.getValue().filters, adjacent.getValue().blockDistance));
		}
		ArrayList<Tuple2<ILogisticsPowerProvider, List<IFilter>>> power = null;
		if (powerAdjacent != null) {
			power = new ArrayList<>(powerAdjacent);
		}
		ArrayList<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower = null;
		if (subSystemPowerAdjacent != null) {
			subSystemPower = new ArrayList<>(subSystemPowerAdjacent);
		}
		if (LPConfiguration.INSTANCE.getThreads() > 0) {
			RoutingTableUpdateThread.add(new LSARouterRunnable(neighboursWithMetric, power, subSystemPower));
		} else {
			lockAndUpdateLSA(neighboursWithMetric, power, subSystemPower);
		}
	}

	private void lockAndUpdateLSA(HashMap<Router, Tuple4<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric, ArrayList<Tuple2<ILogisticsPowerProvider, List<IFilter>>> power, ArrayList<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower) {
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.lock();
		lsa.neighboursWithMetric = neighboursWithMetric;
		lsa.power = power;
		lsa.subSystemPower = subSystemPower;
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.unlock();
	}

	public void CreateRouteTable(int version_to_update_to) {
		CreateRouteTable(version_to_update_to, new DummyRoutingDebugAdapter());
	}

	/**
	 * Create a route table from the link state database
	 */
	public void CreateRouteTable(int version_to_update_to, IRoutingDebugAdapter debug) {

		if (ServerRouter.lastLsaVersion[simpleID] >= version_to_update_to && !debug.independent()) {
			return; // this update is already done.
		}

		//Dijkstra!

		debug.init();

		int routingTableSize = ServerRouter.getBiggestSimpleID();
		if (routingTableSize == 0) {
			routingTableSize = ServerRouter.sharedLsaDatabase.length; // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
		}

		/**
		 * same info as above, but sorted by distance -- sorting is implicit,
		 * because Dijkstra finds the closest routes first.
		 **/
		List<ExitRoute> routeCosts = new ArrayList<>(routingTableSize);

		//Add the current Router
		routeCosts.add(new ExitRoute(this, this, null, null, 0, EnumSet.allOf(PipeRoutingConnectionType.class), 0));

		ArrayList<Tuple2<ILogisticsPowerProvider, List<IFilter>>> powerTable;
		if (powerAdjacent != null) {
			powerTable = new ArrayList<>(powerAdjacent);
		} else {
			powerTable = new ArrayList<>(5);
		}
		ArrayList<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
		if (subSystemPowerAdjacent != null) {
			subSystemPower = new ArrayList<>(subSystemPowerAdjacent);
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
		for (Entry<Router, ExitRoute> pipe : adjacentRouter.entrySet()) {
			ExitRoute currentE = pipe.getValue();
			Router newRouter = pipe.getKey();
			if (newRouter != null) {
				ExitRoute newER = new ExitRoute(newRouter, newRouter, currentE.distanceToDestination, currentE.connectionDetails, currentE.filters, new ArrayList<>(0), currentE.blockDistance);
				candidatesCost.add(newER);
				debug.newCanidate(newER);
			}
		}

		debug.start(candidatesCost, closedSet, filterList);

		ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.lock(); // readlock, not inside the while - too costly to aquire, then release.
		ExitRoute lowestCostNode;
		while ((lowestCostNode = candidatesCost.poll()) != null) {
			if (!lowestCostNode.hasActivePipe()) {
				continue;
			}

			if (debug.isDebug()) {
				ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.unlock();
			}
			debug.nextPipe(lowestCostNode);
			if (debug.isDebug()) {
				ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.lock();
			}

			for (ExitRoute e : candidatesCost) {
				e.debug.isNewlyAddedCanidate = false;
			}

			//if the node does not have any flags not in the closed set, check it
			EnumSet<PipeRoutingConnectionType> lowestCostClosedFlags = closedSet.get(lowestCostNode.destination.getSimpleId());
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

			EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> filters = filterList.get(lowestCostNode.destination.getSimpleId());

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
			if (lowestCostNode.destination.getSimpleId() < ServerRouter.sharedLsaDatabase.length) {
				lsa = ServerRouter.sharedLsaDatabase[lowestCostNode.destination.getSimpleId()];
			}
			if (lsa == null) {
				lowestCostNode.removeFlags(lowestCostClosedFlags);
				lowestCostClosedFlags.addAll(lowestCostNode.getFlagsNoCopy());
				if (lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom)) {
					routeCosts.add(lowestCostNode);
				}
				closedSet.set(lowestCostNode.destination.getSimpleId(), lowestCostClosedFlags);
				continue;
			}
			if (lowestCostNode.containsFlag(PipeRoutingConnectionType.canPowerFrom)) {
				if (lsa.power != null && !lsa.power.isEmpty()) {
					for (Tuple2<ILogisticsPowerProvider, List<IFilter>> p : lsa.power) {
						Tuple2<ILogisticsPowerProvider, List<IFilter>> entry = p.copy();
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
					for (Tuple2<ISubSystemPowerProvider, List<IFilter>> p : lsa.subSystemPower) {
						Tuple2<ISubSystemPowerProvider, List<IFilter>> entry = p.copy();
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
			for (Entry<Router, Tuple4<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> newCandidate : lsa.neighboursWithMetric.entrySet()) {
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
			EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> map = filterList.get(lowestCostNode.destination.getSimpleId());
			if (map == null) {
				map = new EnumMap<>(PipeRoutingConnectionType.class);
				filterList.set(lowestCostNode.destination.getSimpleId(), map);
			}
			for (PipeRoutingConnectionType type : lowestCostNode.getFlagsNoCopy()) {
				if (!map.containsKey(type)) {
					map.put(type, new ArrayList<>());
				}
				map.get(type).add(Collections.unmodifiableList(new ArrayList<>(lowestCostNode.filters)));
			}
			if (lowestCostNode.filters.isEmpty()) {
				closedSet.set(lowestCostNode.destination.getSimpleId(), lowestCostClosedFlags);
			}

			if (debug.isDebug()) {
				ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.unlock();
			}
			debug.handledPipe();
			if (debug.isDebug()) {
				ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.lock();
			}
		}
		ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.unlock();

		debug.stepOneDone();

		//Build route table
		ArrayList<List<ExitRoute>> routeTable = new ArrayList<>(ServerRouter.getBiggestSimpleID() + 1);
		while (simpleID >= routeTable.size()) {
			routeTable.add(null);
		}
		routeTable.set(simpleID, Collections.singletonList(new ExitRoute(this, this, null, null, 0, EnumSet
				.allOf(PipeRoutingConnectionType.class), 0)));

		for (ExitRoute node : routeCosts) {
			Router firstHop = node.root;
			ExitRoute hop = adjacentRouter.get(firstHop);
			if (hop == null) {
				continue;
			}
			node.root = this; // replace the root with this, rather than the first hop.
			node.exitOrientation = hop.exitOrientation;
			while (node.destination.getSimpleId() >= routeTable
					.size()) { // the array will not expand, as it is init'd to contain enough elements
				routeTable.add(null);
			}

			List<ExitRoute> current = routeTable.get(node.destination.getSimpleId());
			if (current != null && !current.isEmpty()) {
				List<ExitRoute> list = new ArrayList<>(current);
				list.add(node);
				routeTable.set(node.destination.getSimpleId(), Collections.unmodifiableList(list));
			} else {
				routeTable.set(node.destination.getSimpleId(), Collections.singletonList(node));
			}
		}
		debug.stepTwoDone();
		if (!debug.independent()) {
			routingTableUpdateWriteLock.lock();
			if (version_to_update_to == lsaVersion) {
				ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.lock();

				if (ServerRouter.lastLsaVersion[simpleID] < version_to_update_to) {
					ServerRouter.lastLsaVersion[simpleID] = version_to_update_to;
					_LPPowerTable = Collections.unmodifiableList(powerTable);
					_SubSystemPowerTable = Collections.unmodifiableList(subSystemPower);
					_routeTable = Collections.unmodifiableList(routeTable);
					_routeCosts = Collections.unmodifiableList(routeCosts);
				}
				ServerRouter.SHARED_LSA_DATABASE_READ_LOCK.unlock();
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
		for (Router r : adjacentRouter.keySet()) {
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
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.lock(); // take a write lock so that we don't overlap with any ongoing route updates
		if (simpleID < ServerRouter.sharedLsaDatabase.length) {
			ServerRouter.sharedLsaDatabase[simpleID] = null;
		}
		ServerRouter.SHARED_LSA_DATABASE_WRITE_LOCK.unlock();
		removeAllInterests();

		clearPipeCache();
		destroyed = true;
		SimpleServiceLocator.routerManager.removeRouter(simpleID);
		for (List<ITileEntityChangeListener> list : listenedPipes) {
			list.remove(localChangeListener);
		}
		updateAdjacentAndLsa();
		ServerRouter.releaseSimpleID(simpleID);
	}

	private void removeAllInterests() {
		removeGenericInterest();
		hasInterestIn.forEach(this::removeInterest);
		hasInterestIn.clear();
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

	@Override
	public void flagForRoutingUpdate() {
		lsaVersion++;
		//if(LogisticsPipes.DEBUG)
		//System.out.println("[LogisticsPipes] targeted for routing update to "+_LSAVersion+" for Node" +  simpleID);
	}

	private void updateAdjacentAndLsa() {
		//this already got a checkAdjacentUpdate, so start the recursion with neighbors
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		IRAction flood = new FloodCheckAdjacent();
		visited.set(simpleID);
		// for all connected updatecurrent and previous
		for (Router r : prevAdjacentRouter.keySet()) {
			r.act(visited, flood);
		}
		for (Router r : adjacentRouter.keySet()) {
			r.act(visited, flood);
		}
		updateLsa();
	}

	private void updateLsa() {
		//now increment LSA version in the network
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		for (Router r : prevAdjacentRouter.keySet()) {
			r.act(visited, new FlagForLSAUpdate());
		}
		prevAdjacentRouter = new HashMap<>();
		act(visited, new FlagForLSAUpdate());
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
			ensureRouteTableIsUpToDate(false);
			return;
		}
		if (LPConfiguration.INSTANCE.getThreads() > 0) {
			ensureRouteTableIsUpToDate(false);
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
			Tuple2<Integer, RouterQueuedTask> element = queue.poll();
			if (element.getValue1() > MainProxy.getGlobalTick()) {
				element.getValue2().call(pipe, this);
			}
		}
	}

	/************* IROUTER *******************/

	@Override
	public boolean isRoutedExit(Direction o) {
		return _routedExits.contains(o);
	}

	@Override
	public boolean isSubPoweredExit(Direction o) {
		return _subPowerExits.containsKey(o);
	}

	@Override
	public int getDistanceToNextPowerPipe(Direction dir) {
		return _subPowerExits.get(dir);
	}

	@Override
	public ExitRoute getExitFor(UUID id, boolean active, ItemStack stack) {
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
	public boolean hasRoute(UUID id, boolean active, ItemStack stack) {
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
	public List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider() {
		return _LPPowerTable;
	}

	@Override
	public List<Tuple2<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider() {
		return _SubSystemPowerTable;
	}

	@Override
	public boolean isSideDisconnected(Direction dir) {
		return null != dir && sideDisconnected[dir.ordinal()];
	}

	@Override
	public void updateInterests() {
		if (--ticksUntilNextInventoryCheck > 0) {
			return;
		}
		ticksUntilNextInventoryCheck = ServerRouter.REFRESH_TIME;
		if (ServerRouter.iterated++ % simpleID == 0) {
			ticksUntilNextInventoryCheck++; // randomly wait 1 extra tick - just so that every router doesn't tick at the same time
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
			newInterests = new TreeSet<>();
		}
		if (!newInterests.equals(hasInterestIn)) {
			for (ItemIdentifier i : hasInterestIn) {
				if (!newInterests.contains(i)) {
					removeInterest(i);
				}
			}
			newInterests.stream().filter(i -> !hasInterestIn.contains(i)).forEach(this::addInterest);
			hasInterestIn = newInterests;
		}
	}

	private void removeGenericInterest() {
		hasGenericInterest = false;
		ServerRouter.genericInterests.remove(this);
	}

	private void declareGenericInterest() {
		hasGenericInterest = true;
		ServerRouter.genericInterests.add(this);
	}

	private void addInterest(ItemIdentifier items) {
		Set<Router> interests = ServerRouter.globalSpecificInterests.get(items);
		if (interests == null) {
			interests = new TreeSet<>();
			ServerRouter.globalSpecificInterests.put(items, interests);
		}
		interests.add(this);
	}

	private void removeInterest(ItemIdentifier p2) {
		Set<Router> interests = ServerRouter.globalSpecificInterests.get(p2);
		if (interests == null) {
			return;
		}
		interests.remove(this);
		if (interests.isEmpty()) {
			ServerRouter.globalSpecificInterests.remove(p2);
		}

	}

	public boolean hasGenericInterest() {
		return hasGenericInterest;
	}

	public boolean hasInterestIn(ItemVariant item) {
		return hasInterestIn.contains(item);
	}

	@Override
	public int compareTo(ServerRouter o) {
		return simpleID - o.simpleID;
	}

	@Override
	public List<ExitRoute> getDistanceTo(Router r) {
		ensureRouteTableIsUpToDate(true);
		int id = r.getSimpleId();
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
		return String.format("ServerRouter: {ID: %d, UUID: %s, AT: (%s, %s), Version: %d), Destroyed: %s}", simpleID, getId(), dim, pos, lsaVersion, isDestroyed());
	}

	@Override
	public void forceLsaUpdate() {
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		act(visited, new FlagForLSAUpdate());
	}

	@Override
	public List<ExitRoute> getRoutersOnSide(Direction direction) {
		return adjacentRouter.values().stream()
				.filter(exit -> exit.exitOrientation == direction)
				.collect(Collectors.toList());
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public void queueTask(int ticks, RouterQueuedTask callable) {
		queue.add(new Tuple2<>(ticks + MainProxy.getGlobalTick(), callable));
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Floodfill recheckAdjacent, leave _prevAdjacentRouter around for LSA
	 * updating
	 */
	static class FloodCheckAdjacent implements IRAction {

		@Override
		public boolean isInteresting(Router that) {
			return that.checkAdjacentUpdate();
		}

		@Override
		public void doTo(Router that) {

		}
	}

	/**
	 * Floodfill LSA increment and clean up the _prevAdjacentRouter list left by
	 * floodCheckAdjacent
	 */
	static class FlagForLSAUpdate implements IRAction {

		@Override
		public boolean isInteresting(Router that) {
			return true;
		}

		@Override
		public void doTo(Router that) {
			that.flagForRoutingUpdate();
		}
	}

	static class FloodClearCache implements IRAction {

		@Override
		public boolean isInteresting(Router that) {
			return true;
		}

		@Override
		public void doTo(Router that) {
			CacheHolder.clearCache(((ServerRouter) that).oldTouchedPipes);
		}
	}

	protected static class LSA {

		public HashMap<Router, Tuple4<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
		public List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> power;
		public ArrayList<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
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

	private class UpdateRouterRunnable extends RouterRunnable {

		int newVersion;
		boolean run;
		Router target;

		UpdateRouterRunnable(Router target) {
			run = true;
			newVersion = lsaVersion;
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
			c = target.getSimpleId() - ((UpdateRouterRunnable) o).target.getSimpleId(); // do things in order of router id, to minimize router recursion
			if (c != 0) {
				return 0;
			}
			c = ((UpdateRouterRunnable) o).newVersion - newVersion; // higher version first
			return c;
		}
	}

	private class LSARouterRunnable extends RouterRunnable {

		private final int index = ServerRouter.maxLSAUpdateIndex++;
		HashMap<Router, Tuple4<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
		ArrayList<Tuple2<ILogisticsPowerProvider, List<IFilter>>> power;
		ArrayList<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;

		LSARouterRunnable(HashMap<Router, Tuple4<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric, ArrayList<Tuple2<ILogisticsPowerProvider, List<IFilter>>> power, ArrayList<Tuple2<ISubSystemPowerProvider, List<IFilter>>> subSystemPower) {
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
