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

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.interfaces.routing.IPowerRouter;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class ServerRouter implements IRouter, IPowerRouter, Comparable<ServerRouter> {
	
	// things with specific interests -- providers (including crafters)
	static HashMap<ItemIdentifier,Set<IRouter>> _globalSpecificInterests = new HashMap<ItemIdentifier,Set<IRouter>>();
	// things potentially interested in every item (chassi with generic sinks)
	static Set<IRouter> _genericInterests = new TreeSet<IRouter>();
	
	// things this pipe is interested in (either providing or sinking)
	Set<ItemIdentifier> _hasInterestIn = new TreeSet();
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

	public HashMap<RoutedPipe, ExitRoute> _adjacent = new HashMap<RoutedPipe, ExitRoute>();
	public HashMap<IRouter, ExitRoute> _adjacentRouter = new HashMap<IRouter, ExitRoute>();
	public List<ILogisticsPowerProvider> _powerAdjacent = new ArrayList<ILogisticsPowerProvider>();
	
	public boolean[] sideDisconnected = new boolean[6];
	
	private HashMap<IRouter, ExitRoute> _prevAdjacentRouter;

	protected static ArrayList<Integer> _lastLSAVersion = new  ArrayList<Integer>();
	protected int _LSAVersion = 0;
	protected LSA _myLsa = new LSA();
	
	protected UpdateRouterRunnable updateThread = null;
	
	protected static RouteLaser _laser = new RouteLaser();

	protected static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
	protected static final Lock SharedLSADatabasereadLock = SharedLSADatabaseLock.readLock();
	protected static final Lock SharedLSADatabasewriteLock = SharedLSADatabaseLock.writeLock();
	protected final ReentrantReadWriteLock routingTableUpdateLock = new ReentrantReadWriteLock();
	protected final Lock routingTableUpdateReadLock = routingTableUpdateLock.readLock();
	protected final Lock routingTableUpdateWriteLock = routingTableUpdateLock.writeLock();
	public Object _externalRoutersByCostLock = new Object();
	
	protected static final ArrayList<LSA> SharedLSADatabase = new ArrayList<LSA>();
	protected static ArrayList<Integer> _lastLsa = new ArrayList<Integer>();
		
	/** Map of router -> orientation for all known destinations **/
	public ArrayList<ExitRoute> _routeTable = new ArrayList<ExitRoute>();
	public List<ExitRoute> _routeCosts = new ArrayList<ExitRoute>();
	public List<ILogisticsPowerProvider> _powerTable = new ArrayList<ILogisticsPowerProvider>();
	public List<IRouter> _externalRoutersByCost = null;
	
	private EnumSet<ForgeDirection> _routedExits = EnumSet.noneOf(ForgeDirection.class);

	private static int firstFreeId = 1;
	private static BitSet simpleIdUsedSet = new BitSet();

	private final int simpleID;
	public final UUID id;
	private int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	
	private WeakReference<CoreRoutedPipe> _myPipeCache=null;
	public void clearPipeCache(){_myPipeCache=null;}
	
	public static void resetStatics() {
		SharedLSADatabasewriteLock.lock();
		SharedLSADatabase.clear();
		SharedLSADatabasewriteLock.unlock();
		Collections.fill(_lastLSAVersion, 0);
		_laser = new RouteLaser();
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
		this.simpleID = claimSimpleID();
		if(SharedLSADatabase.size()<=simpleID){
			SharedLSADatabase.ensureCapacity((int) (simpleID*1.5)); // make structural change
			while(SharedLSADatabase.size()<=(int)simpleID*1.5)
				SharedLSADatabase.add(null);
			_lastLSAVersion.ensureCapacity((int) (simpleID*1.5)); // make structural change
			while(_lastLSAVersion.size()<=(int)simpleID*1.5)
				_lastLSAVersion.add(0);
		}
		_lastLSAVersion.set(this.simpleID,0);
		SharedLSADatabase.set(this.simpleID, _myLsa); // make non-structural change (threadsafe)
		SharedLSADatabasewriteLock.unlock(); 
	}
	
	public int getSimpleID() {
		return this.simpleID;
	}

	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord){
		return _dimension == dimension && _xCoord == xCoord && _yCoord == yCoord && _zCoord == zCoord;
	}

	@Override
	public CoreRoutedPipe getPipe(){
		if(_myPipeCache!=null && _myPipeCache.get()!=null)
			return _myPipeCache.get();
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
		if(_myPipeCache!=null && _myPipeCache.get()!=null)
			return _myPipeCache.get();
		return null;
	}

	private void ensureRouteTableIsUpToDate(boolean force){
		if (_LSAVersion > _lastLSAVersion.get(this.getSimpleID())) {
			if(Configs.multiThreadEnabled && !force) {
				RoutingTableUpdateThread.add(new UpdateRouterRunnable(this));
			} else {
				CreateRouteTable(_LSAVersion);
				_externalRoutersByCost = null;
			}
		}
	}

	@Override
	public ArrayList<ExitRoute> getRouteTable(){
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
		HashMap<RoutedPipe, ExitRoute> adjacent;
		List<ILogisticsPowerProvider> power;
		if(thisPipe instanceof PipeItemsFirewall) {
			adjacent = PathFinder.getConnectedRoutingPipes(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH, ((PipeItemsFirewall)thisPipe).getRouterSide(this));
			power = new ArrayList<ILogisticsPowerProvider>();
		} else {
			adjacent = PathFinder.getConnectedRoutingPipes(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH);
			power = this.getConnectedPowerProvider();
		}
		
		for(RoutedPipe pipe : adjacent.keySet()) {
			if(pipe.stillNeedReplace()) {
				return false;
			}
		}
		
		if(LogisticsPipes.DEBUG) {
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
					pipe.worldObj.notifyBlocksOfNeighborChange(pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.worldObj.getBlockId(pipe.xCoord, pipe.yCoord, pipe.zCoord));
					pipe.refreshConnectionAndRender(false);
				}
			}
		}
		
		for (RoutedPipe pipe : _adjacent.keySet()) {
			if(!adjacent.containsKey(pipe)) {
				adjacentChanged = true;
			}
		}
		
		for (ILogisticsPowerProvider provider : _powerAdjacent){
			if(!power.contains(provider))
				adjacentChanged = true;
		}
		
		for (Entry<RoutedPipe, ExitRoute> pipe : adjacent.entrySet())	{
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
			for(Entry<RoutedPipe,ExitRoute> pipe:adjacent.entrySet()) {
				adjacentRouter.put(((CoreRoutedPipe) pipe.getKey()).getRouter(pipe.getValue().insertOrientation), pipe.getValue());
				if(pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) || pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom))
					routedexits.add(pipe.getValue().exitOrientation);
			}
			_prevAdjacentRouter = _adjacentRouter;
			_adjacentRouter = adjacentRouter;
			_adjacent = adjacent;
			_powerAdjacent = power;
			_routedExits = routedexits;
			SendNewLSA();
		}
		return adjacentChanged;
	}
	
	private void checkSecurity(HashMap<RoutedPipe, ExitRoute> adjacent) {
		CoreRoutedPipe pipe = getPipe();
		if(pipe == null) return;
		UUID id = pipe.getSecurityID();
		List<RoutedPipe> toRemove = new ArrayList<RoutedPipe>();
		if(id != null) {
			for(Entry<RoutedPipe, ExitRoute> entry:adjacent.entrySet()) {
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
			for(Entry<RoutedPipe, ExitRoute> entry:adjacent.entrySet()) {
				if(sideDisconnected[entry.getValue().exitOrientation.ordinal()]) {
					toRemove.add(entry.getKey());
				}
			}
			for(RoutedPipe remove:toRemove) {
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
		
		if(_lastLSAVersion.get(simpleID)>=version_to_update_to)
			return; // this update is already done.

		//Dijkstra!
		
		
		int routingTableSize =ServerRouter.getBiggestSimpleID();
		if(routingTableSize == 0) {
//			routingTableSize=SimpleServiceLocator.routerManager.getRouterCount();
			routingTableSize=SharedLSADatabase.size(); // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
		}

		/** same info as above, but sorted by distance -- sorting is implicit, because Dijkstra finds the closest routes first.**/
		List<ExitRoute> routeCosts = new ArrayList<ExitRoute>(routingTableSize);
		
		ArrayList<ILogisticsPowerProvider> powerTable = new ArrayList<ILogisticsPowerProvider>(_powerAdjacent);
		
		//space and time inefficient, a bitset with 3 bits per node would save a lot but makes the main iteration look like a complete mess
		ArrayList<EnumSet<PipeRoutingConnectionType>> closedSet = new ArrayList<EnumSet<PipeRoutingConnectionType>>(getBiggestSimpleID());
		for(int i=0;i<getBiggestSimpleID();i++)
			closedSet.add(null);
		BitSet objectMapped = new BitSet(routingTableSize);
		objectMapped.set(this.getSimpleID(),true);

		/** The total cost for the candidate route **/
		PriorityQueue<ExitRoute> candidatesCost = new PriorityQueue<ExitRoute>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.
		
		//Init candidates
		// the shortest way to go to an adjacent item is the adjacent item.
		for (Entry<IRouter, ExitRoute> pipe :  _adjacentRouter.entrySet()){
			ExitRoute currentE = pipe.getValue();
			//currentE.connectionDetails.retainAll(blocksPower);
			candidatesCost.add(new ExitRoute(pipe.getKey().getRouter(currentE.insertOrientation), pipe.getKey().getRouter(currentE.insertOrientation), currentE.distanceToDestination, pipe.getValue().connectionDetails));
			//objectMapped.set(pipe.getKey().getSimpleID(),true);
		}

		SharedLSADatabasereadLock.lock(); // readlock, not inside the while - too costly to aquire, then release. 
		ExitRoute lowestCostNode;
		while ((lowestCostNode=candidatesCost.poll()) != null){
			if(!lowestCostNode.hasActivePipe())
				continue;
			//if the node does not have any flags not in the closed set, check it
			EnumSet<PipeRoutingConnectionType> lowestCostClosedFlags = closedSet.get(lowestCostNode.destination.getSimpleID());
			if(lowestCostClosedFlags == null)
				lowestCostClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
			if(lowestCostClosedFlags.containsAll(lowestCostNode.getFlags()))
				continue;
			 
			//Add new candidates from the newly approved route 
			LSA lsa = SharedLSADatabase.get(lowestCostNode.destination.getSimpleID());
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
		    	Entry<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> newCandidate = (Entry<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>>)it.next();
				EnumSet<PipeRoutingConnectionType> newCandidateClosedFlags = closedSet.get(newCandidate.getKey().getSimpleID());
				if(newCandidateClosedFlags == null)
					newCandidateClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
				if(newCandidateClosedFlags.containsAll(newCandidate.getValue().getValue2()))
					continue;

				int candidateCost = lowestCostNode.distanceToDestination + newCandidate.getValue().getValue1();
				EnumSet<PipeRoutingConnectionType> newCT = lowestCostNode.getFlags();
				newCT.retainAll(newCandidate.getValue().getValue2());
				if(!newCT.isEmpty())
					candidatesCost.add(new ExitRoute(lowestCostNode.root, newCandidate.getKey(), candidateCost, newCT));
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
		while (this.getSimpleID() >= routeTable.size())
			routeTable.add(null);
		routeTable.set(this.getSimpleID(), new ExitRoute(this,this, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN,0,EnumSet.noneOf(PipeRoutingConnectionType.class)));

//		List<ExitRoute> noDuplicateRouteCosts = new ArrayList<ExitRoute>(routeCosts.size());
		Iterator<ExitRoute> itr = routeCosts.iterator();
		while (itr.hasNext())
		{
			ExitRoute node = itr.next();
//			if(!node.containsFlag(PipeRoutingConnectionType.canRouteTo))
//				continue;
			IRouter firstHop = node.root;
			ExitRoute hop=_adjacentRouter.get(firstHop);
			if (hop == null){
				continue;
			}
			node.root=this.getRouter(hop.exitOrientation); // replace the root with this, rather than the first hop.
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

			if(_lastLSAVersion.get(simpleID)<version_to_update_to){
				_lastLSAVersion.set(simpleID,version_to_update_to);
				_powerTable = powerTable;
				_routeTable = routeTable;
				_routeCosts = routeCosts; 
			}
			SharedLSADatabasereadLock.unlock();
		}
		routingTableUpdateWriteLock.unlock();
	}
	
	@Override
	public void displayRoutes(){
		_laser.displayRoute(this);
	}
	
	@Override
	public void displayRouteTo(int r){
		_laser.displayRoute(this, r);
	}
	
	@Override
	public void inboundItemArrived(RoutedEntityItem routedEntityItem){
		//notify that Item has arrived
		CoreRoutedPipe pipe = getPipe();	
		if (pipe != null && pipe.logic instanceof IRequireReliableTransport){
			((IRequireReliableTransport)pipe.logic).itemArrived(ItemIdentifierStack.GetFromStack(routedEntityItem.getItemStack()));
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
		if(_prevAdjacentRouter != null) {
			for(IRouter r : _prevAdjacentRouter.keySet()) {
				hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
			}
		}
		actor.doneWith(this);
		return hasBeenReset;
	}
	
	/**
	 * Flags the last sent LSA as expired. Each router will be responsible of purging it from its database.
	 */
	@Override
	public void destroy() {
		SharedLSADatabasewriteLock.lock(); // take a write lock so that we don't overlap with any ongoing route updates
		if (SharedLSADatabase.get(simpleID)!=null) {
			SharedLSADatabase.set(simpleID, null);
		}
		SharedLSADatabasewriteLock.unlock();
		clearPipeCache();
		SimpleServiceLocator.routerManager.removeRouter(this.simpleID);
		updateAdjacentAndLsa();
		releaseSimpleID(simpleID);
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
		if (Configs.multiThreadEnabled) {
			ensureRouteTableIsUpToDate(false);
		}
	}

	/************* IROUTER *******************/
	
	
	@Override
	public void sendRoutedItem(ItemStack item, IRouter destination, Position origin) {
		// TODO Auto-generated method stub
		
	}

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
		if (!SimpleServiceLocator.routerManager.isRouter(id)) return false;
		if(getRouteTable().size()<=id)
			return false;
		ExitRoute source = this.getRouteTable().get(id);
		return source != null && source.containsFlag(PipeRoutingConnectionType.canRouteTo);
	}
	
	@Override
	public ILogisticsModule getLogisticsModule() {
		CoreRoutedPipe pipe = this.getPipe();
		if (pipe == null) return null;
		return pipe.getLogisticsModule();
	}

	@Override
	public List<ILogisticsPowerProvider> getPowerProvider() {
		return _powerTable;
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
		CoreRoutedPipe pipe = getCachedPipe();
		if(pipe==null)
			return null;
		return pipe.getRouter(insertOrientation);
	}

	@Override
	public boolean isSideDisconneceted(ForgeDirection dir) {
		return ForgeDirection.UNKNOWN != dir && sideDisconnected[dir.ordinal()];
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
		Iterator<ItemIdentifier> i2 = _hasInterestIn.iterator();
		
		Set<ItemIdentifier> newInterestPairs = null;
		newInterestPairs = new TreeSet<ItemIdentifier>();
		if(newInterests != null) {
	
			Iterator<ItemIdentifier> i1 = newInterests.iterator();
			while(i1.hasNext() && i2.hasNext()){
				ItemIdentifier p2 = i2.next();
				ItemIdentifier p = i1.next();
				newInterestPairs.add(p);
				if(p.uniqueID != p2.uniqueID){
					this.addInterest(p);
					this.removeInterest(p2);
				}
			}
			while(i1.hasNext()) { // remove extras
				ItemIdentifier items = i1.next();
				newInterestPairs.add(items);
				this.addInterest(items);			
			}
		} 
		while(i2.hasNext()) { // remove extras
			this.removeInterest(i2.next());			
		}
		_hasInterestIn=newInterestPairs;
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
	
	public static Set<IRouter> getRoutersInterestedIn(ItemIdentifier item) {
		Set<IRouter> s = new TreeSet<IRouter>();
		s.addAll(_genericInterests);
		Set<IRouter> specifics = _globalSpecificInterests.get(item);
		if(specifics!=null) {
			s.addAll(specifics);
		}
		specifics = _globalSpecificInterests.get(item.getUndamaged());
		if(specifics!=null) {
			s.addAll(specifics);
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
}



