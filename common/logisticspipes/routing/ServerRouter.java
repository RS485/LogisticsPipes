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
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.Vector;
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
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class ServerRouter implements IRouter, IPowerRouter {
	
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
	public ArrayList<Pair<ForgeDirection, ForgeDirection>> _routeTable = new ArrayList<Pair<ForgeDirection,ForgeDirection>>();
	public List<SearchNode> _routeCosts = new ArrayList<SearchNode>();
	public List<ILogisticsPowerProvider> _powerTable = new ArrayList<ILogisticsPowerProvider>();
	public LinkedList<IRouter> _externalRoutersByCost = null;
	
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
		this.simpleID = claimSimpleID();
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
		SharedLSADatabasereadLock.lock();
		if(SharedLSADatabase.size()<=simpleID){
			SharedLSADatabasereadLock.unlock(); // promote lock type
			SharedLSADatabasewriteLock.lock();
			SharedLSADatabase.ensureCapacity((int) (simpleID*1.5)); // make structural change
			while(SharedLSADatabase.size()<=(int)simpleID*1.5)
				SharedLSADatabase.add(null);
			_lastLSAVersion.ensureCapacity((int) (simpleID*1.5)); // make structural change
			while(_lastLSAVersion.size()<=(int)simpleID*1.5)
				_lastLSAVersion.add(0);
			SharedLSADatabasewriteLock.unlock(); // demote lock
			SharedLSADatabasereadLock.lock();
		}
		_lastLSAVersion.set(this.simpleID,0);
		SharedLSADatabase.set(this.simpleID, _myLsa); // make non-structural change (threadsafe)
		SharedLSADatabasereadLock.unlock();
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
	public ArrayList<Pair<ForgeDirection,ForgeDirection>> getRouteTable(){
		ensureRouteTableIsUpToDate(true);
		return _routeTable;
	}
	
	@Override
	public List<SearchNode> getIRoutersByCost() {
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
			power = new LinkedList<ILogisticsPowerProvider>();
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
		
		for (RoutedPipe pipe : adjacent.keySet())	{
			if (!_adjacent.containsKey(pipe)){
				adjacentChanged = true;
				break;
			}
			ExitRoute newExit = adjacent.get(pipe);
			ExitRoute oldExit = _adjacent.get(pipe);
			
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
		for (IRouter adjacent : _adjacentRouter.keySet()){
			neighboursWithMetric.put(adjacent, new Pair<Integer, EnumSet<PipeRoutingConnectionType>>(_adjacentRouter.get(adjacent).metric, _adjacentRouter.get(adjacent).connectionDetails));
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
	private void CreateRouteTable(int version_to_update_to)	{
		
		if(_lastLSAVersion.get(simpleID)>=version_to_update_to)
			return; // this update is already done.

		//Dijkstra!
		
		
		int routingTableSize =ServerRouter.getBiggestSimpleID();
		if(routingTableSize == 0) {
//			routingTableSize=SimpleServiceLocator.routerManager.getRouterCount();
			routingTableSize=SharedLSADatabase.size(); // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
		}

		/** same info as above, but sorted by distance -- sorting is implicit, because Dijkstra finds the closest routes first.**/
		ArrayList<SearchNode> routeCosts = new ArrayList<SearchNode>(routingTableSize);
		
		ArrayList<ILogisticsPowerProvider> powerTable = new ArrayList<ILogisticsPowerProvider>(_powerAdjacent);
		
		//space and time inefficient, a bitset with 3 bits per node would save a lot but makes the main iteration look like a complete mess
		Vector<EnumSet<PipeRoutingConnectionType>> closedSet = new Vector<EnumSet<PipeRoutingConnectionType>>(getBiggestSimpleID());
		closedSet.setSize(getBiggestSimpleID());
		BitSet objectMapped = new BitSet(routingTableSize);
		objectMapped.set(this.getSimpleID(),true);

		/** The total cost for the candidate route **/
		PriorityQueue<SearchNode> candidatesCost = new PriorityQueue<SearchNode>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.
		
		//Init candidates
		// the shortest way to go to an adjacent item is the adjacent item.
		for (Entry<IRouter, ExitRoute> pipe :  _adjacentRouter.entrySet()){
			ExitRoute currentE = pipe.getValue();
			//currentE.connectionDetails.retainAll(blocksPower);
			candidatesCost.add(new SearchNode(pipe.getKey().getRouter(currentE.insertOrientation), currentE.metric, pipe.getValue().connectionDetails, pipe.getKey().getRouter(currentE.insertOrientation)));
			//objectMapped.set(pipe.getKey().getSimpleID(),true);
		}

		SharedLSADatabasereadLock.lock(); // readlock, not inside the while - too costly to aquire, then release. 
		SearchNode lowestCostNode;
		while ((lowestCostNode=candidatesCost.poll()) != null){
			if(!lowestCostNode.hasActivePipe())
				continue;
			//if the node does not have any flags not in the closed set, check it
			EnumSet<PipeRoutingConnectionType> lowestCostClosedFlags = closedSet.get(lowestCostNode.node.getSimpleID());
			if(lowestCostClosedFlags == null)
				lowestCostClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
			if(lowestCostClosedFlags.containsAll(lowestCostNode.getFlags()))
				continue;
			 
			//Add new candidates from the newly approved route 
			LSA lsa = SharedLSADatabase.get(lowestCostNode.node.getSimpleID());
			if(lsa == null) {
				lowestCostNode.removeFlags(lowestCostClosedFlags);
				lowestCostClosedFlags.addAll(lowestCostNode.getFlags());
				if(lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom))
					routeCosts.add(lowestCostNode);
				closedSet.set(lowestCostNode.node.getSimpleID(),lowestCostClosedFlags);
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

				int candidateCost = lowestCostNode.distance + newCandidate.getValue().getValue1();
				EnumSet<PipeRoutingConnectionType> newCT = lowestCostNode.getFlags();
				newCT.retainAll(newCandidate.getValue().getValue2());
				if(!newCT.isEmpty())
					candidatesCost.add(new SearchNode(newCandidate.getKey(), candidateCost, newCT, lowestCostNode.root));
			}

			lowestCostNode.removeFlags(lowestCostClosedFlags);
			lowestCostClosedFlags.addAll(lowestCostNode.getFlags());
			if(lowestCostNode.containsFlag(PipeRoutingConnectionType.canRouteTo) || lowestCostNode.containsFlag(PipeRoutingConnectionType.canRequestFrom))
				routeCosts.add(lowestCostNode);
			closedSet.set(lowestCostNode.node.getSimpleID(),lowestCostClosedFlags);
		}
		SharedLSADatabasereadLock.unlock();
		
		
		//Build route table
		ArrayList<Pair<ForgeDirection, ForgeDirection>> routeTable = new ArrayList<Pair<ForgeDirection,ForgeDirection>>(ServerRouter.getBiggestSimpleID()+1);
		while (this.getSimpleID() >= routeTable.size())
			routeTable.add(null);
		routeTable.set(this.getSimpleID(), new Pair<ForgeDirection,ForgeDirection>(ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN));
		for (SearchNode node : routeCosts)
		{
			if(!node.containsFlag(PipeRoutingConnectionType.canRouteTo))
				continue;
			IRouter firstHop = node.root;
			if (firstHop == null) { //this should never happen?!?
				while (node.node.getSimpleID() >= routeTable.size())
					routeTable.add(null);
				routeTable.set(node.node.getSimpleID(), new Pair<ForgeDirection,ForgeDirection>(ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN));
				continue;
			}
			ExitRoute hop=_adjacentRouter.get(firstHop);
			
			if (hop == null){
				continue;
			}
			while (node.node.getSimpleID() >= routeTable.size())
				routeTable.add(null);
			routeTable.set(node.node.getSimpleID(), new Pair<ForgeDirection,ForgeDirection>(hop.exitOrientation, hop.insertOrientation));
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
	public void itemDropped(RoutedEntityItem routedEntityItem) {
		//TODO
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
		return this.getRouteTable().get(id).getValue1();
	}
	
	@Override
	public boolean hasRoute(int id) {
		if (!SimpleServiceLocator.routerManager.isRouter(id)) return false;
		if(getRouteTable().size()<=id)
			return false;
		return this.getRouteTable().get(id)!=null;
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
}


