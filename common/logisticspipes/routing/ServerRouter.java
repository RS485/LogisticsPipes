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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import logisticspipes.config.Configs;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.interfaces.routing.IPowerRouter;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.pipes.PipeItemsBasicLogistics;
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

	public static final EnumSet<PipeRoutingConnectionType> blocksPower = EnumSet.of(PipeRoutingConnectionType.blocksPowerFlow, PipeRoutingConnectionType.passedThroughIronClosed, PipeRoutingConnectionType.passedThroughObsidian);
	public static final EnumSet<PipeRoutingConnectionType> blocksRouting = EnumSet.of(PipeRoutingConnectionType.blocksItemFlow, PipeRoutingConnectionType.reversedPassedThroughIronClosed, PipeRoutingConnectionType.passedThroughDiamond, PipeRoutingConnectionType.passedThroughObsidian);
	public static final EnumSet<PipeRoutingConnectionType> blocksItems = EnumSet.of(PipeRoutingConnectionType.blocksItemFlow, PipeRoutingConnectionType.passedThroughIronClosed, PipeRoutingConnectionType.passedThroughDiamond, PipeRoutingConnectionType.passedThroughObsidian);
	//may not speed up the code - consumes about 7% of CreateRouteTable runtume
	@Override 
	public int hashCode(){
		return (int)id.getLeastSignificantBits(); // RandomID is cryptographcially secure, so this is a good approximation of true random.
	}
	
	private class LSA {
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

	private static int _LSDVersion = 0;
	private int _lastLSDVersion = 0;
	
	private RoutingUpdateThread updateThread = null;
	
	private static RouteLaser _laser = new RouteLaser();

	private static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
	private static final Lock SharedLSADatabasereadLock = SharedLSADatabaseLock.readLock();
	private static final Lock SharedLSADatabasewriteLock = SharedLSADatabaseLock.writeLock();
	private static final ReentrantReadWriteLock updateThreadLock = new ReentrantReadWriteLock();
	private static final Lock updateThreadreadLock = updateThreadLock.readLock();
	private static final Lock updateThreadwriteLock = updateThreadLock.writeLock();
	public Object _externalRoutersByCostLock = new Object();
	
	private static final HashMap<IRouter,LSA> SharedLSADatabase = new HashMap<IRouter,LSA>();
	private LSA _myLsa = new LSA();
		
	/** Map of router -> orientation for all known destinations **/
	public HashMap<IRouter, ForgeDirection> _routeTable = new HashMap<IRouter, ForgeDirection>();
	public List<SearchNode> _routeCosts = new ArrayList<SearchNode>();
	public List<ILogisticsPowerProvider> _powerTable = new ArrayList<ILogisticsPowerProvider>();
	public LinkedList<IRouter> _externalRoutersByCost = null;

	private boolean _blockNeedsUpdate;
	private boolean forceUpdate = true;
	
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
	}
	
	public ServerRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord){
		this.id = id;
		this._dimension = dimension;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
		clearPipeCache();
		_myLsa = new LSA();
		_myLsa.neighboursWithMetric = new HashMap<IRouter, Pair<Integer,EnumSet<PipeRoutingConnectionType>>>();
		_myLsa.power = new ArrayList<ILogisticsPowerProvider>();
		SharedLSADatabasewriteLock.lock();
		SharedLSADatabase.put(this, _myLsa);
		SharedLSADatabasewriteLock.unlock();
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
	public HashMap<IRouter, ForgeDirection> getRouteTable(){
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
	private void recheckAdjacent() {
		boolean adjacentChanged = false;
		CoreRoutedPipe thisPipe = getPipe();
		if (thisPipe == null) return;
		HashMap<RoutedPipe, ExitRoute> adjacent = PathFinder.getConnectedRoutingPipes(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH);
		List<ILogisticsPowerProvider> power = this.getConnectedPowerProvider();
		
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
			for(Entry<RoutedPipe,ExitRoute> pipe:adjacent.entrySet()) {
				adjacentRouter.put(((CoreRoutedPipe) pipe.getKey()).getRouter(), pipe.getValue());
			}
			_adjacentRouter = adjacentRouter;
			_adjacent = adjacent;
			_powerAdjacent = power;
			_blockNeedsUpdate = true;
			SendNewLSA();
		}
	}
	
	private void SendNewLSA() {
		HashMap<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> neighboursWithMetric = new HashMap<IRouter, Pair<Integer,EnumSet<PipeRoutingConnectionType>>>();
		ArrayList<ILogisticsPowerProvider> power = new ArrayList<ILogisticsPowerProvider>();
		for (RoutedPipe adjacent : _adjacent.keySet()){
			neighboursWithMetric.put(adjacent.getRouter(), new Pair<Integer, EnumSet<PipeRoutingConnectionType>>(_adjacent.get(adjacent).metric, _adjacent.get(adjacent).connectionDetails));
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
		
		int routingTableSize = _routeTable.size()+1;
		if(routingTableSize == 0) {
//			routingTableSize=SimpleServiceLocator.routerManager.getRouterCount();
			routingTableSize=SharedLSADatabase.size()+1; // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
		}
		/** Map of all "approved" routers and the route to get there **/
		HashMap<IRouter,SearchNode> tree =  new HashMap<IRouter,SearchNode>(routingTableSize);

		/** same info as above, but sorted by distance -- sorting is implicit, because Dijkstra finds the closest routes first.**/
		ArrayList<SearchNode> routeCosts = new ArrayList<SearchNode>(routingTableSize);
		
		ArrayList<ILogisticsPowerProvider> powerTable = new ArrayList<ILogisticsPowerProvider>(_powerAdjacent);
		
		//Init root(er - lol)
		// the shortest way to yourself is to go nowhere
		tree.put(this,new SearchNode(this,0,EnumSet.noneOf(PipeRoutingConnectionType.class),null));

		/** The total cost for the candidate route **/
		PriorityQueue<SearchNode> candidatesCost = new PriorityQueue<SearchNode>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.
		
		//Init candidates
		// the shortest way to go to an adjacent item is the adjacent item.
		for (Entry<RoutedPipe, ExitRoute> pipe :  _adjacent.entrySet()){
			ExitRoute currentE = pipe.getValue();
			//currentE.connectionDetails.retainAll(blocksPower);
			candidatesCost.add(new SearchNode(pipe.getKey().getRouter(), currentE.metric, pipe.getValue().connectionDetails, pipe.getKey().getRouter()));
		}

		SearchNode lowestCostNode;
		while ((lowestCostNode=candidatesCost.poll()) != null){
			
			while(lowestCostNode!=null && tree.containsKey(lowestCostNode.node)) // the node was inserted multiple times, skip it as we know a shorter path.
				lowestCostNode=candidatesCost.poll();
			if(lowestCostNode==null)
				break; // then there was nothing but already routed elements in the list.
			
			SearchNode lowestPath = tree.get(lowestCostNode.parent);						//Get a copy of the route for the approved router 

			if(lowestPath == null) { // then you are on an initial seed item, you are adjacent, and you are the closest node.
				lowestPath = lowestCostNode;
			}

			
			//Add new candidates from the newly approved route
			SharedLSADatabasereadLock.lock();
			LSA lsa = SharedLSADatabase.get(lowestCostNode.node);
			if(lsa != null) {
				if(!lowestCostNode.connectionFlags.contains(PipeRoutingConnectionType.blocksPowerFlow) && lsa.power.isEmpty() == false) {
					powerTable.addAll(lsa.power);
				}
			    Iterator it = lsa.neighboursWithMetric.entrySet().iterator();
			    while (it.hasNext()) {
			    	Entry<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>> newCandidate = (Entry<IRouter, Pair<Integer, EnumSet<PipeRoutingConnectionType>>>)it.next();
					SearchNode treeN = tree.get(newCandidate.getKey());
					if (treeN != null) {
						if(!EnumSet.copyOf(lowestCostNode.connectionFlags).removeAll(this.blocksPower)) {
							treeN.connectionFlags.removeAll(blocksPower);
						}
						continue;
					}
					int candidateCost = lowestCostNode.distance + newCandidate.getValue().getValue1();
					EnumSet<PipeRoutingConnectionType> newCT = EnumSet.copyOf(lowestCostNode.connectionFlags);
					newCT.addAll(lowestPath.connectionFlags);
					candidatesCost.add(new SearchNode(newCandidate.getKey(), candidateCost, newCT, lowestPath.node));
				}
			}
			lowestCostNode.parent=lowestPath.parent; // however you got here, thats the side you came from
			//Approve the candidate
			tree.put(lowestCostNode.node,lowestCostNode);

			routeCosts.add(lowestCostNode);
			
			SharedLSADatabasereadLock.unlock();
		}
		
		
		//Build route table
		HashMap<IRouter, ForgeDirection> routeTable = new HashMap<IRouter, ForgeDirection>(tree.size());

		for (SearchNode node : tree.values())
		{
			IRouter firstHop = node.parent;
			if (firstHop == null) {
				routeTable.put(node.node, ForgeDirection.UNKNOWN);
				continue;
			}
			ExitRoute hop=_adjacentRouter.get(firstHop);
			
			if (hop == null){
				continue;
			}
			routeTable.put(node.node, hop.exitOrientation);
		}
		_powerTable = powerTable;
		_routeTable = routeTable;
		_routeCosts = routeCosts;
	}
	
	private LinkedList<ForgeDirection> GetNonRoutedExits()	{
		LinkedList<ForgeDirection> ret = new LinkedList<ForgeDirection>();
		
		outer:
		for (int i = 0 ; i < 6; i++){
			ForgeDirection o = ForgeDirection.values()[i];
			for(ExitRoute route : _adjacent.values()) {
				if (route.exitOrientation == o){
					continue outer; //Its routed
				}
			}
			ret.add(o);	//Its not (might not be a pipe, but its not routed)
		}
		return ret;
	}
	
	@Override
	public void displayRoutes(){
		_laser.displayRoute(this);
	}
	
	@Override
	public void displayRouteTo(IRouter r){
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
		SharedLSADatabasewriteLock.lock();
		if (SharedLSADatabase.containsKey(this)) {
			SharedLSADatabase.remove(this);
			_LSDVersion++;
		}
		SharedLSADatabasewriteLock.unlock();
		SimpleServiceLocator.routerManager.removeRouter(this.id);
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
		return !GetNonRoutedExits().contains(o);
	}

	@Override
	public ForgeDirection getExitFor(UUID id) {
		return this.getRouteTable().get(SimpleServiceLocator.routerManager.getRouter(id));
	}
	
	@Override
	public boolean hasRoute(UUID id) {
		if (!SimpleServiceLocator.routerManager.isRouter(id)) return false;
		
		IRouter r = SimpleServiceLocator.routerManager.getRouter(id);
		
		if (!this.getRouteTable().containsKey(r)) return false;
		
		return true;
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
}


