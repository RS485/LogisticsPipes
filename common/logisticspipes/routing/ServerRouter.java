/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class ServerRouter implements IRouter, IPowerRouter {

	private class LSA {
		public HashMap<IRouter, Pair<Integer,Boolean>> neighboursWithMetric;
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
	public HashMap<IRouter, Pair<Integer,Boolean>> _routeCosts = new HashMap<IRouter, Pair<Integer,Boolean>>();
	public List<ILogisticsPowerProvider> _powerTable = new ArrayList<ILogisticsPowerProvider>();
	public LinkedList<IRouter> _externalRoutersByCost = null;

	private boolean _blockNeedsUpdate;
	private boolean forceUpdate = true;
	
	public final UUID id;
	private int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	
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
		_myLsa = new LSA();
		_myLsa.neighboursWithMetric = new HashMap<IRouter, Pair<Integer,Boolean>>();
		_myLsa.power = new ArrayList<ILogisticsPowerProvider>();
		SharedLSADatabasewriteLock.lock();
		SharedLSADatabase.put(this, _myLsa);
		SharedLSADatabasewriteLock.unlock();
	}

	@Override
	public CoreRoutedPipe getPipe(){
		World worldObj = MainProxy.getWorld(_dimension);
		if(worldObj == null) {
			return null;
		}
		TileEntity tile = worldObj.getBlockTileEntity(_xCoord, _yCoord, _zCoord);
		
		if (!(tile instanceof TileGenericPipe)) return null;
		TileGenericPipe pipe = (TileGenericPipe) tile;
		if (!(pipe.pipe instanceof CoreRoutedPipe)) return null;
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
	public LinkedList<IRouter> getIRoutersByCost() {
		ensureRouteTableIsUpToDate(true);
		synchronized (_externalRoutersByCostLock) {
			if (_externalRoutersByCost  == null){
				LinkedList<IRouter> externalRoutersByCost = new LinkedList<IRouter>();
				
				LinkedList<RouterCost> tempList = new LinkedList<RouterCost>();
				outer:
				for (IRouter r : _routeCosts.keySet()){
					for (int i = 0; i < tempList.size(); i++){
						if (_routeCosts.get(r).getValue1() < tempList.get(i).cost){
							tempList.add(i, new RouterCost(r, _routeCosts.get(r).getValue1()));
							continue outer;
						}
					}
					tempList.addLast(new RouterCost(r, _routeCosts.get(r).getValue1()));
				}
				
				while(tempList.size() > 0){
					externalRoutersByCost.addLast(tempList.removeFirst().router);
				}
				externalRoutersByCost.addFirst(this);
				_externalRoutersByCost = externalRoutersByCost;
			}		
			return _externalRoutersByCost;
		}
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
			
			if (newExit.exitOrientation != oldExit.exitOrientation || newExit.metric != oldExit.metric || newExit.isPipeLess != oldExit.isPipeLess)	{
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
			for(RoutedPipe pipe:adjacent.keySet()) {
				adjacentRouter.put(pipe.getRouter(), adjacent.get(pipe));
			}
			_adjacentRouter = adjacentRouter;
			_adjacent = adjacent;
			_powerAdjacent = power;
			_blockNeedsUpdate = true;
			SendNewLSA();
		}
	}
	
	private void SendNewLSA() {
		HashMap<IRouter, Pair<Integer, Boolean>> neighboursWithMetric = new HashMap<IRouter, Pair<Integer,Boolean>>();
		ArrayList<ILogisticsPowerProvider> power = new ArrayList<ILogisticsPowerProvider>();
		for (RoutedPipe adjacent : _adjacent.keySet()){
			neighboursWithMetric.put(adjacent.getRouter(), new Pair<Integer, Boolean>(_adjacent.get(adjacent).metric, _adjacent.get(adjacent).isPipeLess));
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
		
		/** Map of all "approved" routers and the route to get there **/
		HashMap<IRouter, IRouter> tree =  new HashMap<IRouter, IRouter>();
		/** The cost to get to an "approved" router **/
		HashMap<IRouter, Pair<Integer,Boolean>> treeCost = new HashMap<IRouter, Pair<Integer,Boolean>>();
		
		ArrayList<ILogisticsPowerProvider> powerTable = new ArrayList<ILogisticsPowerProvider>(_powerAdjacent);
		
		//Init root(er - lol)
		tree.put(this, null);
		treeCost.put(this, new Pair<Integer, Boolean>(0, false));
		/** The candidate router and which approved router put it in the candidate list **/
		HashMap<IRouter, IRouter> candidates = new HashMap<IRouter, IRouter>();
		/** The total cost for the candidate route **/
		HashMap<IRouter, Pair<Integer,Boolean>> candidatesCost = new HashMap<IRouter, Pair<Integer,Boolean>>();
		
		//Init candidates
		for (RoutedPipe pipe :  _adjacent.keySet()){
			candidates.put(pipe.getRouter(), this);
			candidatesCost.put(pipe.getRouter(), new Pair<Integer, Boolean>(_adjacent.get(pipe).metric,_adjacent.get(pipe).isPipeLess));
		}

		
		while (!candidates.isEmpty()){
			IRouter lowestCostCandidateRouter = null;
			int lowestCost = Integer.MAX_VALUE;
			boolean isPipeLess = true;
			for(IRouter candidate : candidatesCost.keySet()){
				if (candidatesCost.get(candidate).getValue1() < lowestCost){
					lowestCostCandidateRouter = candidate;
					lowestCost = candidatesCost.get(candidate).getValue1();
					isPipeLess = candidatesCost.get(candidate).getValue2();
				}
			}
			
			IRouter lowestParent = candidates.get(lowestCostCandidateRouter);	//Get the approved parent of the lowest cost candidate
			IRouter lowestPath = tree.get(lowestParent);						//Get a copy of the route for the approved router 
			if(lowestPath == null) {
				lowestPath = lowestCostCandidateRouter;
			}
			
			//Approve the candidate
			tree.put(lowestCostCandidateRouter, lowestPath);
			treeCost.put(lowestCostCandidateRouter, new Pair<Integer, Boolean>(lowestCost,isPipeLess));
			
			//Remove from candidate list
			candidates.remove(lowestCostCandidateRouter);
			candidatesCost.remove(lowestCostCandidateRouter);
			
			//Add new candidates from the newly approved route
			SharedLSADatabasereadLock.lock();
			LSA lsa = SharedLSADatabase.get(lowestCostCandidateRouter);
			if(lsa != null) {
				if(!isPipeLess) {
					powerTable.addAll(lsa.power);
				}
				for (IRouter newCandidate: lsa.neighboursWithMetric.keySet()){
					if (tree.containsKey(newCandidate)) {
						if(treeCost.get(newCandidate).getValue2() && !isPipeLess) {
							treeCost.get(newCandidate).setValue2(false);
						}
						continue;
					}
					int candidateCost = lowestCost + lsa.neighboursWithMetric.get(newCandidate).getValue1();
					if (candidates.containsKey(newCandidate) && candidatesCost.get(newCandidate).getValue1() <= candidateCost){
						continue;
					}
					candidates.put(newCandidate, lowestCostCandidateRouter);
					candidatesCost.put(newCandidate, new Pair<Integer, Boolean>(candidateCost, isPipeLess || lsa.neighboursWithMetric.get(newCandidate).getValue2()));
				}
			}
			SharedLSADatabasereadLock.unlock();
		}
		
		
		//Build route table
		HashMap<IRouter, ForgeDirection> routeTable = new HashMap<IRouter, ForgeDirection>();
		HashMap<IRouter, Pair<Integer, Boolean>> routeCosts = new HashMap<IRouter, Pair<Integer, Boolean>>();
		for (IRouter node : tree.keySet())
		{
			IRouter firstHop = tree.get(node);
			if (firstHop == null) {
				routeTable.put(node, ForgeDirection.UNKNOWN);
				continue;
			}
			
			if (!_adjacentRouter.containsKey(firstHop) || _adjacentRouter.get(firstHop) == null){
				continue;
			}
			
			routeCosts.put(node, treeCost.get(node));
			routeTable.put(node, _adjacentRouter.get(firstHop).exitOrientation);
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
			((IRequireReliableTransport)pipe.logic).itemArrived(ItemIdentifier.get(routedEntityItem.getItemStack()));
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


