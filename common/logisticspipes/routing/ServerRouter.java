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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.transport.TileGenericPipe;

public class ServerRouter implements IRouter, Comparable<ServerRouter> {
	
	// things with specific interests -- providers (including crafters)
	static HashMap<ItemIdentifier,Set<IRouter>> _globalSpecificInterests = new HashMap<ItemIdentifier,Set<IRouter>>();
	// things potentially interested in every item (chassi with generic sinks)
	static Set<IRouter> _genericInterests = new TreeSet<IRouter>();
	
	// things this pipe is interested in (either providing or sinking)
	Set<ItemIdentifier> _hasInterestIn = new TreeSet<ItemIdentifier>();
	boolean _hasGenericInterest;
	
	static final int REFRESH_TIME=20;
	static int iterated=0;// used pseudp-random to spread items over the tick range
	int ticksUntillNextInventoryCheck=0;
	@Override 
	public int hashCode(){
		return simpleID; // guaranteed to be unique, and uniform distribution over a range.
	}
	
	protected class LSA {
		public HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> neighboursWithMetric;
		public List<ILogisticsPowerProvider> power;
	}
	
	private class UpdateRouterRunnable implements Comparable<UpdateRouterRunnable>, Runnable {
		
		int newVersion = 0;
		boolean run = false;
		IRouter target;
		UpdateRouterRunnable(IRouter target) {
			run = true;
			newVersion = _LSAVersion;
			this.target=target;
		}
		
		@Override
		public void run() {
			if(!run) return;
			try {
				CoreRoutedPipe p = target.getCachedPipe();
				if(p==null){
					run = false;
					return;
				}
				//spinlock during the first tick, we can't touch the routing table, untill Update() has been called on every pipe.
				while(p.stillNeedReplace()){Thread.sleep(10);}
				CreateRouteTable(newVersion);
			} catch(Exception e) {
				e.printStackTrace();
			}
			run = false;
		}

		@Override
		public int compareTo(UpdateRouterRunnable o) {
			int c=0;
			if(o.newVersion<=0)
				c = newVersion-o.newVersion; // negative numbers have priority, more negative first
			if(c!=0) return 0;
				c = this.target.getSimpleID()-o.target.getSimpleID(); // do things in order of router id, to minimize router recursion
			if(c!=0) return 0;
				c = o.newVersion - newVersion; // higher version first
			return c;
		}		
	}

	// these are maps, not hashMaps because they are unmodifiable Collections to avoid concurrentModification exceptions.
	public Map<CoreRoutedPipe, ExitRoute> _adjacent = new HashMap<CoreRoutedPipe, ExitRoute>();
	public Map<IRouter, ExitRoute> _adjacentRouter = new HashMap<IRouter, ExitRoute>();
	public List<ILogisticsPowerProvider> _powerAdjacent = new ArrayList<ILogisticsPowerProvider>();
	
	public boolean[] sideDisconnected = new boolean[6];
	
	protected Map<IRouter, ExitRoute> _prevAdjacentRouter = new HashMap<IRouter, ExitRoute>();

	protected static int[] _lastLSAVersion = new int[0];
	protected int _LSAVersion = 0;
	protected LSA _myLsa = new LSA();

	protected UpdateRouterRunnable updateThread = null;

	protected static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
	protected static final Lock SharedLSADatabasereadLock = SharedLSADatabaseLock.readLock();
	protected static final Lock SharedLSADatabasewriteLock = SharedLSADatabaseLock.writeLock();
	protected final ReentrantReadWriteLock routingTableUpdateLock = new ReentrantReadWriteLock();
	protected final Lock routingTableUpdateReadLock = routingTableUpdateLock.readLock();
	protected final Lock routingTableUpdateWriteLock = routingTableUpdateLock.writeLock();
	public Object _externalRoutersByCostLock = new Object();
	
	protected static LSA[] SharedLSADatabase = new LSA[0];

	/** Map of router -> orientation for all known destinations **/
	public List<ExitRoute> _routeTable = new ArrayList<ExitRoute>();
	public List<ExitRoute> _routeCosts = new ArrayList<ExitRoute>();
	public List<ILogisticsPowerProvider> _powerTable = new ArrayList<ILogisticsPowerProvider>();
	public List<IRouter> _firewallRouter = new ArrayList<IRouter>();
	
	private EnumSet<ForgeDirection> _routedExits = EnumSet.noneOf(ForgeDirection.class);

	private static int firstFreeId = 1;
	private static BitSet simpleIdUsedSet = new BitSet();

	protected final int simpleID;
	public final UUID id;
	private int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	
	@Getter
	@Setter(value=AccessLevel.PRIVATE)
	private boolean destroied = false;
	
	private WeakReference<CoreRoutedPipe> _myPipeCache=null;
	@Override
	public void clearPipeCache(){_myPipeCache=null;}
	
	// called on server shutdown only
	public static void cleanup() {
		_globalSpecificInterests.clear();
		_genericInterests.clear();
		SharedLSADatabasewriteLock.lock();
		SharedLSADatabase = new LSA[0];
		_lastLSAVersion = new int[0];
		SharedLSADatabasewriteLock.unlock();
		simpleIdUsedSet.clear();
		firstFreeId = 1;
	}
	
	private static int claimSimpleID() {
		int idx = simpleIdUsedSet.nextClearBit(firstFreeId);
		firstFreeId = idx + 1;
		simpleIdUsedSet.set(idx);
		return idx;
	}
	
	private static void releaseSimpleID(int idx) {
		simpleIdUsedSet.clear(idx);
		if(idx < firstFreeId)
			firstFreeId = idx;
	}
	
	public static int getBiggestSimpleID() {
		return simpleIdUsedSet.size();
	}
	
	public ServerRouter(UUID globalID, int dimension, int xCoord, int yCoord, int zCoord){
		if(globalID!=null)
			this.id = globalID;
		else
			this.id = UUID.randomUUID();
		this._dimension = dimension;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
		clearPipeCache();
		_myLsa = new LSA();
		_myLsa.neighboursWithMetric = new HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>>();
		_myLsa.power = new ArrayList<ILogisticsPowerProvider>();
		SharedLSADatabasewriteLock.lock(); // any time after we claim the SimpleID, the database could be accessed at that index
		simpleID = claimSimpleID();
		if(SharedLSADatabase.length<=simpleID){
			int newlength = ((int) (simpleID*1.5))+1;
			LSA[] new_SharedLSADatabase = new LSA[newlength];
			System.arraycopy(SharedLSADatabase, 0, new_SharedLSADatabase, 0, SharedLSADatabase.length);
			SharedLSADatabase = new_SharedLSADatabase;
			int[] new_lastLSAVersion = new int[newlength];
			System.arraycopy(_lastLSAVersion, 0, new_lastLSAVersion, 0, _lastLSAVersion.length);
			_lastLSAVersion = new_lastLSAVersion;
		}
		_lastLSAVersion[simpleID] = 0;
		SharedLSADatabase[simpleID] = _myLsa; // make non-structural change (threadsafe)
		SharedLSADatabasewriteLock.unlock(); 
	}
	
	@Override
	public int getSimpleID() {
		return this.simpleID;
	}

	@Override
	public boolean isInDim(int dimension) {
		return _dimension == dimension;
	}

	@Override
	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord){
		return _dimension == dimension && _xCoord == xCoord && _yCoord == yCoord && _zCoord == zCoord;
	}

	@Override
	public CoreRoutedPipe getPipe(){
		CoreRoutedPipe crp = getCachedPipe();
		if(crp != null)
			return crp;
		World worldObj = DimensionManager.getWorld(_dimension);
		if(worldObj == null) {
			return null;
		}
		TileEntity tile = worldObj.getBlockTileEntity(_xCoord, _yCoord, _zCoord);
		
		if (!(tile instanceof TileGenericPipe)) return null;
		TileGenericPipe pipe = (TileGenericPipe) tile;
		if (!(pipe.pipe instanceof CoreRoutedPipe)) return null;
		_myPipeCache=new WeakReference<CoreRoutedPipe>((CoreRoutedPipe) pipe.pipe);

		return (CoreRoutedPipe) pipe.pipe;
	}

	@Override
	public CoreRoutedPipe getCachedPipe(){
		if(_myPipeCache!=null)
			return _myPipeCache.get();
		return null;
	}

	@Override
	public boolean isValidCache() {
		return getPipe() != null;
	}

	private void ensureRouteTableIsUpToDate(boolean force){
		if (_LSAVersion > _lastLSAVersion[simpleID]) {
			if(Configs.MULTI_THREAD_NUMBER > 0 && !force) {
				RoutingTableUpdateThread.add(new UpdateRouterRunnable(this));
			} else {
				CreateRouteTable(_LSAVersion);
			}
		}
	}

	@Override
	public List<ExitRoute> getRouteTable(){
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
		return this.id;
	}
	

	/**
	 * Rechecks the piped connection to all adjacent routers as well as discover new ones.
	 */
	private boolean recheckAdjacent() {
		boolean adjacentChanged = false;
		CoreRoutedPipe thisPipe = getPipe();
		if (thisPipe == null) return false;
		HashMap<CoreRoutedPipe, ExitRoute> adjacent;
		List<ILogisticsPowerProvider> power;
		if(thisPipe instanceof PipeItemsFirewall) {
			adjacent = PathFinder.getConnectedRoutingPipes(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH, ((PipeItemsFirewall)thisPipe).getRouterSide(this));
			power = new ArrayList<ILogisticsPowerProvider>();
		} else {
			adjacent = PathFinder.getConnectedRoutingPipes(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH);
			power = this.getConnectedPowerProvider();
		}
		
		for(CoreRoutedPipe pipe : adjacent.keySet()) {
			if(pipe.stillNeedReplace()) {
				return false;
			}
		}
		
		boolean[] oldSideDisconnected = sideDisconnected;
		sideDisconnected = new boolean[6];
		checkSecurity(adjacent);
		
		boolean changed = false;
		
		for(int i=0;i<6;i++) {
			changed |= sideDisconnected[i] != oldSideDisconnected[i];
		}
		if(changed) {
			CoreRoutedPipe pipe = getPipe();
			if (pipe != null) {
				pipe.getWorld().notifyBlocksOfNeighborChange(pipe.getX(), pipe.getY(), pipe.getZ(), pipe.getWorld().getBlockId(pipe.getX(), pipe.getY(), pipe.getZ()));
				pipe.refreshConnectionAndRender(false);
			}
			adjacentChanged = true;
		}
		
		if(_adjacent.size() != adjacent.size()) {
			adjacentChanged = true;
		}
		
		for (CoreRoutedPipe pipe : _adjacent.keySet()) {
			if(!adjacent.containsKey(pipe)) {
				adjacentChanged = true;
			}
		}
		
		for (ILogisticsPowerProvider provider : _powerAdjacent){
			if(!power.contains(provider))
				adjacentChanged = true;
		}
		
		for (Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet())	{
			ExitRoute oldExit = _adjacent.get(pipe.getKey());
			if (oldExit==null){
				adjacentChanged = true;
				break;
			}
			ExitRoute newExit = pipe.getValue();
			
			if (!newExit.equals(oldExit))	{
				adjacentChanged = true;
				break;
			}
		}
		
		for (ILogisticsPowerProvider provider : power){
			if(!_powerAdjacent.contains(provider))
				adjacentChanged = true;
		}
		
		if (adjacentChanged) {
			HashMap<IRouter, ExitRoute> adjacentRouter = new HashMap<IRouter, ExitRoute>();
			EnumSet<ForgeDirection> routedexits = EnumSet.noneOf(ForgeDirection.class);
			for(Entry<CoreRoutedPipe,ExitRoute> pipe:adjacent.entrySet()) {
				adjacentRouter.put(pipe.getKey().getRouter(pipe.getValue().insertOrientation), pipe.getValue());
				if(pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) || pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom))
					routedexits.add(pipe.getValue().exitOrientation);
			}
			HashMap<IRouter, ExitRoute> oldRouters = new HashMap<IRouter, ExitRoute>(_adjacentRouter);
			for(IRouter key:adjacentRouter.keySet())
				oldRouters.remove(key);
			_prevAdjacentRouter = Collections.unmodifiableMap(oldRouters);
			_adjacentRouter = Collections.unmodifiableMap(adjacentRouter);
			_adjacent = Collections.unmodifiableMap(adjacent);
			_powerAdjacent = Collections.unmodifiableList(power);
			_routedExits = routedexits;
			SendNewLSA();
		}
		return adjacentChanged;
	}
	
	private void checkSecurity(HashMap<CoreRoutedPipe, ExitRoute> adjacent) {
		CoreRoutedPipe pipe = getPipe();
		if(pipe == null) return;
		UUID id = pipe.getSecurityID();
		List<CoreRoutedPipe> toRemove = new ArrayList<CoreRoutedPipe>();
		if(id != null) {
			for(Entry<CoreRoutedPipe, ExitRoute> entry:adjacent.entrySet()) {
				if(!entry.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) && !entry.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom)) continue;
				UUID thatId = entry.getKey().getSecurityID();
				if(!(pipe instanceof PipeItemsFirewall)) {
					if(thatId == null) {
						entry.getKey().insetSecurityID(id);
					} else if(!id.equals(thatId)) {
						sideDisconnected[entry.getValue().exitOrientation.ordinal()] = true;
					}
				} else {
					if(!(entry.getKey() instanceof PipeItemsFirewall)) {
						if(thatId != null && !id.equals(thatId)) {
							sideDisconnected[entry.getValue().exitOrientation.ordinal()] = true;
						}
					}
				}
			}
			for(Entry<CoreRoutedPipe, ExitRoute> entry:adjacent.entrySet()) {
				if(sideDisconnected[entry.getValue().exitOrientation.ordinal()]) {
					toRemove.add(entry.getKey());
				}
			}
			for(CoreRoutedPipe remove:toRemove) {
				adjacent.remove(remove);
			}
		}
	}

	private void SendNewLSA() {
		HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> neighboursWithMetric = new HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>>();
		ArrayList<ILogisticsPowerProvider> power = new ArrayList<ILogisticsPowerProvider>();
		for (Entry<IRouter, ExitRoute> adjacent : _adjacentRouter.entrySet()){
			neighboursWithMetric.put(adjacent.getKey(), new Pair<Integer, EnumSet<PipeRoutingConnectionType>>(adjacent.getValue().distanceToDestination, adjacent.getValue().connectionDetails));
		}
		for (ILogisticsPowerProvider provider : _powerAdjacent){
			power.add(provider);
		}
		SharedLSADatabasewriteLock.lock();
		_myLsa.neighboursWithMetric = neighboursWithMetric;
		_myLsa.power = power;
		SharedLSADatabasewriteLock.unlock();
	}
	
	/**
	 * Create a route table from the link state database
	 */
	protected void CreateRouteTable(int version_to_update_to)	{
		
		if(_lastLSAVersion[simpleID] >= version_to_update_to)
			return; // this update is already done.

		//Dijkstra!
		
		
		int routingTableSize =ServerRouter.getBiggestSimpleID();
		if(routingTableSize == 0) {
//			routingTableSize=SimpleServiceLocator.routerManager.getRouterCount();
			routingTableSize=SharedLSADatabase.length; // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
		}

		/** same info as above, but sorted by distance -- sorting is implicit, because Dijkstra finds the closest routes first.**/
		List<ExitRoute> routeCosts = new ArrayList<ExitRoute>(routingTableSize);
		
		ArrayList<ILogisticsPowerProvider> powerTable = new ArrayList<ILogisticsPowerProvider>(_powerAdjacent);
		ArrayList<IRouter> firewallRouter = new ArrayList<IRouter>();
		
		//space and time inefficient, a bitset with 3 bits per node would save a lot but makes the main iteration look like a complete mess
		ArrayList<EnumSet<PipeRoutingConnectionType>> closedSet = new ArrayList<EnumSet<PipeRoutingConnectionType>>(getBiggestSimpleID());
		for(int i=0;i<getBiggestSimpleID();i++)
			closedSet.add(null);
		BitSet objectMapped = new BitSet(routingTableSize);
		objectMapped.set(simpleID, true);

		/** The total cost for the candidate route **/
		PriorityQueue<ExitRoute> candidatesCost = new PriorityQueue<ExitRoute>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.
		
		//Init candidates
		// the shortest way to go to an adjacent item is the adjacent item.
		for (Entry<IRouter, ExitRoute> pipe :  _adjacentRouter.entrySet()){
			//currentE.connectionDetails.retainAll(blocksPower);
			ExitRoute currentE = pipe.getValue();
			IRouter newRouter = pipe.getKey();
			if(newRouter != null){
				ExitRoute newER = new ExitRoute(newRouter, newRouter, currentE.distanceToDestination, currentE.connectionDetails);
				candidatesCost.add(newER);
			}
			//objectMapped.set(pipe.getKey().getSimpleID(),true);
		}

		SharedLSADatabasereadLock.lock(); // readlock, not inside the while - too costly to aquire, then release. 
		ExitRoute lowestCostNode;
		while ((lowestCostNode = candidatesCost.poll()) != null){
			if(!lowestCostNode.hasActivePipe())
				continue;

			if(lowestCostNode.destination instanceof IFilteringRouter) {
				firewallRouter.add(lowestCostNode.destination);
			}
			
			//if the node does not have any flags not in the closed set, check it
			EnumSet<PipeRoutingConnectionType> lowestCostClosedFlags = closedSet.get(lowestCostNode.destination.getSimpleID());
			if(lowestCostClosedFlags == null)
				lowestCostClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
			if(lowestCostClosedFlags.containsAll(lowestCostNode.getFlags()))
				continue;
			
			//Add new candidates from the newly approved route 
			LSA lsa = null;
			if(lowestCostNode.destination.getSimpleID() < SharedLSADatabase.length) {
				lsa = SharedLSADatabase[lowestCostNode.destination.getSimpleID()];
			}
			if(lsa == null) {
				lowestCostNode.removeFlags(lowestCostClosedFlags);
				lowestCostClosedFlags.addAll(lowestCostNode.getFlags());
				if(lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom))
					routeCosts.add(lowestCostNode);
				closedSet.set(lowestCostNode.destination.getSimpleID(),lowestCostClosedFlags);
				continue;
			}
			
			if(lowestCostNode.containsFlag(PipeRoutingConnectionType.canPowerFrom)) {
				if(lsa.power.isEmpty() == false) {
					if(!lowestCostClosedFlags.contains(PipeRoutingConnectionType.canPowerFrom)) {
						powerTable.addAll(lsa.power);
					}
				}
			}
			
		    Iterator<Entry<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>>> it = lsa.neighboursWithMetric.entrySet().iterator();
		    while (it.hasNext()) {
		    	Entry<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> newCandidate = it.next();
				EnumSet<PipeRoutingConnectionType> newCandidateClosedFlags = closedSet.get(newCandidate.getKey().getSimpleID());
				if(newCandidateClosedFlags == null)
					newCandidateClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
				if(newCandidateClosedFlags.containsAll(newCandidate.getValue().getValue2()))
					continue;

				int candidateCost = lowestCostNode.distanceToDestination + newCandidate.getValue().getValue1();
				EnumSet<PipeRoutingConnectionType> newCT = lowestCostNode.getFlags();
				newCT.retainAll(newCandidate.getValue().getValue2());
				if(!newCT.isEmpty()) {
					candidatesCost.add(new ExitRoute(lowestCostNode.root, newCandidate.getKey(), candidateCost, newCT));
				}
			}

			lowestCostNode.removeFlags(lowestCostClosedFlags);
			lowestCostClosedFlags.addAll(lowestCostNode.getFlags());
			if(lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom))
				routeCosts.add(lowestCostNode);
			closedSet.set(lowestCostNode.destination.getSimpleID(),lowestCostClosedFlags);
		}
		SharedLSADatabasereadLock.unlock();
		
		
		//Build route table
		ArrayList<ExitRoute> routeTable = new ArrayList<ExitRoute>(ServerRouter.getBiggestSimpleID()+1);
		while (simpleID >= routeTable.size())
			routeTable.add(null);
		routeTable.set(simpleID, new ExitRoute(this,this, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN,0,EnumSet.allOf(PipeRoutingConnectionType.class)));

//		List<ExitRoute> noDuplicateRouteCosts = new ArrayList<ExitRoute>(routeCosts.size());
		Iterator<ExitRoute> itr = routeCosts.iterator();
		while (itr.hasNext())
		{
			ExitRoute node = itr.next();
//			if(!node.containsFlag(PipeRoutingConnectionType.canRouteTo))
//				continue;
			IRouter firstHop = node.root;
			ExitRoute hop = _adjacentRouter.get(firstHop);
			if (hop == null){
				continue;
			}
			node.root = this.getRouter(hop.exitOrientation); // replace the root with this, rather than the first hop.
			node.exitOrientation = hop.exitOrientation;
			node.insertOrientation = hop.insertOrientation;
			while (node.destination.getSimpleID() >= routeTable.size()) // the array will not expand, as it is init'd to contain enough elements
				routeTable.add(null);
			
			ExitRoute current = routeTable.get(node.destination.getSimpleID());
			if(current!=null){
				ExitRoute merged = new ExitRoute(current,node);
				routeTable.set(merged.destination.getSimpleID(), merged);
				//				itr.remove();
			}else
				routeTable.set(node.destination.getSimpleID(), node);
			
		}
		routingTableUpdateWriteLock.lock();
		if(version_to_update_to==this._LSAVersion){
			SharedLSADatabasereadLock.lock();

			if(_lastLSAVersion[simpleID] < version_to_update_to){
				_lastLSAVersion[simpleID] = version_to_update_to;
				_powerTable = Collections.unmodifiableList(powerTable);
				_routeTable = Collections.unmodifiableList(routeTable);
				_routeCosts = Collections.unmodifiableList(routeCosts); 
				_firewallRouter = Collections.unmodifiableList(firewallRouter);
			}
			SharedLSADatabasereadLock.unlock();
		}
		routingTableUpdateWriteLock.unlock();
	}
	
	@Override
	public void inboundItemArrived(RoutedEntityItem routedEntityItem){
		//notify that Item has arrived
		CoreRoutedPipe pipe = getPipe();	
		pipe.notifyOfItemArival(routedEntityItem);
		if (pipe != null && pipe instanceof IRequireReliableTransport){
			((IRequireReliableTransport)pipe).itemArrived(ItemIdentifierStack.GetFromStack(routedEntityItem.getItemStack()));
		}
		if (pipe != null && pipe instanceof IRequireReliableFluidTransport) {
			ItemStack stack = routedEntityItem.getItemStack();
			if(stack.getItem() instanceof LogisticsFluidContainer) {
				FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
				((IRequireReliableFluidTransport)pipe).liquidArrived(FluidIdentifier.get(liquid), liquid.amount);				
			}
		}
	}

	@Override
	public boolean act(BitSet hasBeenProcessed,IRAction actor){
		boolean hasBeenReset=false;
		if(hasBeenProcessed.get(this.simpleID))
			return hasBeenReset;
		hasBeenProcessed.set(this.simpleID);
		if(!actor.isInteresting(this))
			return hasBeenReset;
		if(actor.doTo(this)){
			hasBeenProcessed.clear();
			// don't need to worry about resetting the recursion, as we are the neighbour of our neighbour, and are no longer flaged as processed.
			hasBeenReset=true;
		}
		for(IRouter r : _adjacentRouter.keySet()) {
			hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
		}
		for(IRouter r : _prevAdjacentRouter.keySet()) {
			hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
		}
		return hasBeenReset;
	}
	
	/**
	 * Flags the last sent LSA as expired. Each router will be responsible of purging it from its database.
	 */
	@Override
	public void destroy() {
		SharedLSADatabasewriteLock.lock(); // take a write lock so that we don't overlap with any ongoing route updates
		if (simpleID < SharedLSADatabase.length) {
			SharedLSADatabase[simpleID] = null;
		}
		SharedLSADatabasewriteLock.unlock();
		this.removeAllInterests();
		
		clearPipeCache();
		setDestroied(true);
		SimpleServiceLocator.routerManager.removeRouter(this.simpleID);
		updateAdjacentAndLsa();
		releaseSimpleID(simpleID);
	}


	private void removeAllInterests() {
		this.removeGenericInterest();
		for(ItemIdentifier i : _hasInterestIn) {
			this.removeInterest(i);
		}
		_hasInterestIn.clear();
	}

	/**
	 * Floodfill recheckAdjacent, leave _prevAdjacentRouter around for LSA updating
	 */
	class floodCheckAdjacent implements IRAction{
		@Override
		public boolean isInteresting(IRouter that) {
			return that.checkAdjacentUpdate();
		}
		@Override
		public boolean doTo(IRouter that) {
			return false;
		}
		@Override
		public void doneWith(IRouter that) {
		}
	}

	@Override
	public boolean checkAdjacentUpdate() {
		boolean blockNeedsUpdate = recheckAdjacent();
		if(!blockNeedsUpdate) return false;

		CoreRoutedPipe pipe = getPipe();
		if (pipe == null) return true;
		pipe.refreshRender(true);
		return true;
	}

	/**
	 * Floodfill LSA increment and clean up the _prevAdjacentRouter list left by floodCheckAdjacent
	 */
	class flagForLSAUpdate implements IRAction{
		@Override
		public boolean isInteresting(IRouter that) {
			return true;
		}
		@Override
		public boolean doTo(IRouter that) {
			that.flagForRoutingUpdate();
			return false;
		}
		@Override
		public void doneWith(IRouter that) {
			that.clearPrevAdjacent();
		}
	}

	@Override
	public void flagForRoutingUpdate() {
		_LSAVersion++;
		//if(LogisticsPipes.DEBUG)
			//System.out.println("[LogisticsPipes] flag for routing update to "+_LSAVersion+" for Node" +  simpleID);
	}

	@Override
	public void clearPrevAdjacent() {
		_prevAdjacentRouter = null;
	}


	private void updateAdjacentAndLsa() {
		//this already got a checkAdjacentUpdate, so start the recursion with neighbors
		BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
		IRAction flood = new floodCheckAdjacent();
		visited.set(simpleID);
		for(IRouter r : _adjacentRouter.keySet()) {
			r.act(visited, flood);
		}
		if(_prevAdjacentRouter != null) {
			for(IRouter r : _prevAdjacentRouter.keySet()) {
				r.act(visited, flood);
			}
		}
		//now increment LSA version in the network and clean up _prevAdjacentRouter
		visited.clear();
		this.act(visited, new flagForLSAUpdate());
	}
	
	@Override
	public void update(boolean doFullRefresh){	
		
		updateInterests();
		if (doFullRefresh) {
			boolean blockNeedsUpdate = checkAdjacentUpdate();
			if (blockNeedsUpdate) {
				updateAdjacentAndLsa();
			}
			ensureRouteTableIsUpToDate(false);
			CoreRoutedPipe pipe = getPipe();
			if (pipe != null) {
				pipe.refreshRender(false);
			}
			return;
		}
		if (Configs.MULTI_THREAD_NUMBER > 0) {
			ensureRouteTableIsUpToDate(false);
		}
	}

	/************* IROUTER *******************/

	@Override
	public boolean isRoutedExit(ForgeDirection o){
		return _routedExits.contains(o);
	}

	@Override
	public ForgeDirection getExitFor(int id) {
		return this.getRouteTable().get(id).exitOrientation;
	}
	
	@Override
	public boolean hasRoute(int id) {
		if (!SimpleServiceLocator.routerManager.isRouterUnsafe(id,false)) return false;
		if(getRouteTable().size()<=id)
			return false;
		ExitRoute source = this.getRouteTable().get(id);
		return source != null && source.containsFlag(PipeRoutingConnectionType.canRouteTo);
	}
	
	@Override
	public LogisticsModule getLogisticsModule() {
		CoreRoutedPipe pipe = this.getPipe();
		if (pipe == null) return null;
		return pipe.getLogisticsModule();
	}

	@Override
	public List<ILogisticsPowerProvider> getPowerProvider() {
		return Collections.unmodifiableList(_powerTable);
	}
	
	private List<ILogisticsPowerProvider> getConnectedPowerProvider() {
		CoreRoutedPipe pipe = getPipe();
		if(pipe instanceof PipeItemsBasicLogistics) {
			return ((PipeItemsBasicLogistics)pipe).getConnectedPowerProviders();
		} else {
			return new ArrayList<ILogisticsPowerProvider>();
		}
	}
	
	@Override
	public IRouter getRouter(ForgeDirection insertOrientation) {
		return this;
	}

	@Override
	public boolean isSideDisconneceted(ForgeDirection dir) {
		return ForgeDirection.UNKNOWN != dir && sideDisconnected[dir.ordinal()];
	}

	@Override
	public List<IRouter> getFilteringRouter() {
		return Collections.unmodifiableList(_firewallRouter);
	}

	@Override
	public void updateInterests() {
		if(--ticksUntillNextInventoryCheck>0)
			return;
		ticksUntillNextInventoryCheck=REFRESH_TIME;
		if(iterated++%this.simpleID==0)
			ticksUntillNextInventoryCheck++; // randomly wait 1 extra tick - just so that every router doesn't tick at the same time
		CoreRoutedPipe pipe = getPipe();
		if(pipe==null)
			return;
		if(pipe.hasGenericInterests())
			this.declareGenericInterest();
		else
			this.removeGenericInterest();
		Set<ItemIdentifier> newInterests = pipe.getSpecificInterests();
		if(newInterests == null) {
			newInterests = new TreeSet<ItemIdentifier>();
		}
		if(!newInterests.equals(_hasInterestIn)) {
			for(ItemIdentifier i : _hasInterestIn) {
				if(!newInterests.contains(i)) {
					this.removeInterest(i);
				}
			}
			for(ItemIdentifier i : newInterests) {
				if(!_hasInterestIn.contains(i)) {
					this.addInterest(i);
				}
			}
			_hasInterestIn=newInterests;
		}
	}

	private void removeGenericInterest() {
		this._hasGenericInterest=false;
		_genericInterests.remove(this);
	}

	private void declareGenericInterest() {
		this._hasGenericInterest=true;
		_genericInterests.add(this);
	}

	private void addInterest(ItemIdentifier items) {
		Set<IRouter> interests = _globalSpecificInterests.get(items);
		if(interests==null) {
			interests = new TreeSet<IRouter>();
			_globalSpecificInterests.put(items, interests);
		}
		interests.add(this);		
	}

	private void removeInterest(ItemIdentifier p2) {
		Set<IRouter> interests = _globalSpecificInterests.get(p2);
		if(interests==null) {
			return;
		}
		interests.remove(this);
		if(interests.isEmpty())
			_globalSpecificInterests.remove(interests);
		
	}

	public boolean hasGenericInterest() {
		return this._hasGenericInterest;
	}
	
	public boolean hasInterestIn(ItemIdentifier item) {
		return this._hasInterestIn.contains(item);
	}
	
	public static BitSet getRoutersInterestedIn(ItemIdentifier item) {
		BitSet s = new BitSet(getBiggestSimpleID()+1);
		if(_genericInterests != null){
			for(IRouter r:_genericInterests){
				s.set(r.getSimpleID());
			}
		}
		Set<IRouter> specifics = _globalSpecificInterests.get(item);
		if(specifics != null){
			for(IRouter r:specifics){
				s.set(r.getSimpleID());
			}
		}
		specifics = _globalSpecificInterests.get(item.getUndamaged());
		if(specifics != null){
			for(IRouter r:specifics){
				s.set(r.getSimpleID());
			}
		}
		specifics = _globalSpecificInterests.get(item.getIgnoringNBT());
		if(specifics != null){
			for(IRouter r:specifics){
				s.set(r.getSimpleID());
			}
		}
		return s;
	}

	@Override
	public int compareTo(ServerRouter o) {
		return this.simpleID-o.simpleID;
	}

	@Override
	public ExitRoute getDistanceTo(IRouter r) {
		ensureRouteTableIsUpToDate(true);
		int id = r.getSimpleID();
		if (_routeTable.size()<=id) return null;
		return _routeTable.get(id);
	}

	public static Map<ItemIdentifier,Set<IRouter>> getInterestedInSpecifics() {
		return _globalSpecificInterests;		
	}

	public static Set<IRouter> getInterestedInGeneral() {
		return _genericInterests;
	}

	@Override
	public void clearInterests() {
		this.removeAllInterests();		
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder("ServerRouter: {ID: ");
		string.append(simpleID);
		string.append(", UUID: ");
		string.append(this.getId());
		string.append(", AT: (");
		string.append(this._dimension);
		string.append(", ");
		string.append(this._xCoord);
		string.append(", ");
		string.append(this._yCoord);
		string.append(", ");
		string.append(this._zCoord);
		string.append("), Version: ");
		string.append(_LSAVersion);
		string.append("), Destroied: ");
		string.append(isDestroied());
		return string.append("}").toString();
	}
}



