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

import logisticspipes.config.Configs;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.interfaces.routing.IPowerRouter;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class ServerRouter implements IRouter, IPowerRouter {
	
	//may not speed up the code - consumes about 7% of CreateRouteTable runtume
	@Override 
	public int hashCode(){
		return simpleID; // guaranteed to be unique, and uniform distribution over a range.
//		return (int)id.getLeastSignificantBits(); // RandomID is cryptographcially secure, so this is a good approximation of true random.
	}
	
	protected class LSA {
		public HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> neighboursWithMetric;
		public List<ILogisticsPowerProvider> power;
	}
	
	private class RoutingUpdateThread implements Runnable {
		
		int newVersion = 0;
		boolean run = false;
		RoutingUpdateThread(int version) {
			newVersion = version;
			run = true;
		}
		
		@Override
		public void run() {
			if(!run) return;
			try {
				CreateRouteTable();
				synchronized (_externalRoutersByCostLock) {
					_externalRoutersByCost = null;
				}
				_lastLSDVersion = newVersion;
			} catch(Exception e) {
				e.printStackTrace();
			}
			run = false;
			updateThreadwriteLock.lock();
			updateThread = null;
			updateThreadwriteLock.unlock();
		}
		
	}

	public HashMap<RoutedPipe, ExitRoute> _adjacent = new HashMap<RoutedPipe, ExitRoute>();
	public HashMap<IRouter, ExitRoute> _adjacentRouter = new HashMap<IRouter, ExitRoute>();
	public List<ILogisticsPowerProvider> _powerAdjacent = new ArrayList<ILogisticsPowerProvider>();

	protected static int _LSDVersion = 0;
	protected int _lastLSDVersion = 0;
	
	protected RoutingUpdateThread updateThread = null;
	
	protected static RouteLaser _laser = new RouteLaser();

	protected static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
	protected static final Lock SharedLSADatabasereadLock = SharedLSADatabaseLock.readLock();
	protected static final Lock SharedLSADatabasewriteLock = SharedLSADatabaseLock.writeLock();
	protected static final ReentrantReadWriteLock updateThreadLock = new ReentrantReadWriteLock();
	protected static final Lock updateThreadreadLock = updateThreadLock.readLock();
	protected static final Lock updateThreadwriteLock = updateThreadLock.writeLock();
	public Object _externalRoutersByCostLock = new Object();
	
	protected static final ArrayList<LSA> SharedLSADatabase = new ArrayList<LSA>();
	protected LSA _myLsa = new LSA();
		
	/** Map of router -> orientation for all known destinations **/
	public ArrayList<Pair<ForgeDirection, ForgeDirection>> _routeTable = new ArrayList<Pair<ForgeDirection,ForgeDirection>>();
	public List<SearchNode> _routeCosts = new ArrayList<SearchNode>();
	public List<ILogisticsPowerProvider> _powerTable = new ArrayList<ILogisticsPowerProvider>();
	public LinkedList<IRouter> _externalRoutersByCost = null;
	
	private EnumSet<ForgeDirection> _routedExits = EnumSet.noneOf(ForgeDirection.class);

	protected boolean _blockNeedsUpdate;
	private boolean forceUpdate = true;

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
		_LSDVersion = 0;
		_laser = new RouteLaser();
		simpleIdUsedSet.clear();
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
			while(SharedLSADatabase.size()<=simpleID)
				SharedLSADatabase.add(null);
			SharedLSADatabasewriteLock.unlock(); // demote lock
			SharedLSADatabasereadLock.lock();
		}
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
		World worldObj = MainProxy.getWorld(_dimension);
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

	private void ensureRouteTableIsUpToDate(boolean force){
		if (_LSDVersion > _lastLSDVersion) {
			if(Configs.multiThreadEnabled && !force && SimpleServiceLocator.routerManager.routerAddingDone()) {
				updateThreadreadLock.lock();
				if(updateThread != null) {
					if(updateThread.newVersion == _LSDVersion) {
						updateThreadreadLock.unlock();
						return;
					}
					updateThread.run = false;
					RoutingTableUpdateThread.remove(updateThread);
				}
				updateThreadreadLock.unlock();
				updateThread = new RoutingUpdateThread(_LSDVersion);
				if(_lastLSDVersion == 0) {
					RoutingTableUpdateThread.addPriority(updateThread);
				} else {
					RoutingTableUpdateThread.add(updateThread);
				}
			} else {
				CreateRouteTable();
				_externalRoutersByCost = null;
				_lastLSDVersion = _LSDVersion;
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
	protected void recheckAdjacent() {
		boolean adjacentChanged = false;
		CoreRoutedPipe thisPipe = getPipe();
		if (thisPipe == null) return;
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
				return;
			}
		}
		
		for (RoutedPipe pipe : _adjacent.keySet()){
			if(!adjacent.containsKey(pipe))
				adjacentChanged = true;
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
			_routedExits.clear();
			for(Entry<RoutedPipe,ExitRoute> pipe:adjacent.entrySet()) {
				adjacentRouter.put(((CoreRoutedPipe) pipe.getKey()).getRouter(pipe.getValue().insertOrientation), pipe.getValue());
				if(pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRouteTo) || pipe.getValue().connectionDetails.contains(PipeRoutingConnectionType.canRequestFrom))
					_routedExits.add(pipe.getValue().exitOrientation);
			}
			_adjacentRouter = adjacentRouter;
			_adjacent = adjacent;
			_powerAdjacent = power;
			_blockNeedsUpdate = true;
			SendNewLSA();
		}
	}
	
	protected void SendNewLSA() {
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
		_LSDVersion++;
		SharedLSADatabasewriteLock.unlock();
	}
	
	/**
	 * Create a route table from the link state database
	 */
	private void CreateRouteTable()	{ 
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
		BitSet objectMapped = new BitSet(routingTableSize);
		objectMapped.set(this.getPipe().getRouter().getSimpleID(),true);

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
		_powerTable = powerTable;
		_routeTable = routeTable;
		_routeCosts = routeCosts;
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
	
	/**
	 * Flags the last sent LSA as expired. Each router will be responsible of purging it from its database.
	 */
	@Override
	public void destroy() {
		SharedLSADatabasewriteLock.lock(); // take a write lock so that we don't overlap with any ongoing route updates
		if (SharedLSADatabase.get(simpleID)!=null) {
			SharedLSADatabase.set(simpleID, null);
			_LSDVersion++;
		}
		SharedLSADatabasewriteLock.unlock();
		SimpleServiceLocator.routerManager.removeRouter(this.simpleID);
		releaseSimpleID(simpleID);
		updateNeighbors();
	}

	private void updateNeighbors() {
		for(RoutedPipe p : _adjacent.keySet()) {
			p.getRouter().update(true);
		}
	}
	
	@Override
	public void update(boolean doFullRefresh){
		if (doFullRefresh || forceUpdate) {
			if(updateThread == null) {
				forceUpdate = false;
				recheckAdjacent();
				if (_blockNeedsUpdate){
					CoreRoutedPipe pipe = getPipe();
					if (pipe == null) return;
					pipe.worldObj.markBlockForRenderUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);
					pipe.refreshRender(true);
					_blockNeedsUpdate = false;
					updateNeighbors();
				}
			} else {
				forceUpdate = true;
			}
		}
		ensureRouteTableIsUpToDate(false);
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
	
	@Override
	public List<ILogisticsPowerProvider> getConnectedPowerProvider() {
		CoreRoutedPipe pipe = getPipe();
		if(pipe instanceof PipeItemsBasicLogistics) {
			return ((PipeItemsBasicLogistics)pipe).getConnectedPowerProviders();
		} else {
			return new ArrayList<ILogisticsPowerProvider>();
		}
	}

	@Override
	public IRouter getRouter(ForgeDirection insertOrientation) {
		CoreRoutedPipe pipe = getPipe();
		if(pipe==null)
			return null;
		return pipe.getRouter(insertOrientation);
	}
}


