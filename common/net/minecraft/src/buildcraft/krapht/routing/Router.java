/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.routing;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.IRequireReliableTransport;
import net.minecraft.src.buildcraft.krapht.PipeTransportLogistics;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.DimensionManager;
import net.minecraft.src.krapht.ItemIdentifier;

public class Router implements IRouter{

	public class LSA {
		public Router source;
		public HashMap<Router, Integer> neighboursWithMetric;
	}
	
	public HashMap<RoutedPipe, ExitRoute> _adjacent = new HashMap<RoutedPipe, ExitRoute>();
	private final LinkedList<RoutedEntityItem> _outboundItems = new LinkedList<RoutedEntityItem>();
	private final LinkedList<RoutedEntityItem> _inboundItems = new LinkedList<RoutedEntityItem>();

	private static int _LSDVersion = 0;
	private int _lastLSDVersion = 0;
	
	private static RouteLaser _laser = new RouteLaser();
	
	private static LinkedList<LSA> SharedLSADatabase = new LinkedList<Router.LSA>();
	private LSA _myLsa = new LSA();
		
	/** Map of router -> orientation for all known destinations **/
	private HashMap<Router, Orientations> _routeTable = new HashMap<Router, Orientations>();
	private HashMap<Router, Integer> _routeCosts = new HashMap<Router, Integer>();
	private LinkedList<Router> _routersByCost = null;
	private LinkedList<IRouter> _externalRoutersByCost = null;

	private boolean _blockNeedsUpdate;
	
	public final UUID id;
	private final int _dimensionId;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	
	public static void ResetStatics(){
		SharedLSADatabase.clear();
		_LSDVersion = 0;
		_laser = new RouteLaser();
	}
	
	public Router(UUID id, int dimensionId, int xCoord, int yCoord, int zCoord){
		this.id = id;
		this._dimensionId = dimensionId;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
		_myLsa = new LSA();
		_myLsa.source = this;
		_myLsa.neighboursWithMetric = new HashMap<Router, Integer>();
		SharedLSADatabase.add(_myLsa);
	}
	
	@Deprecated
	public CoreRoutedPipe getPipe(){
		//TODO Check if this works
		TileEntity tile = DimensionManager.getProvider(_dimensionId).worldObj.getBlockTileEntity(_xCoord, _yCoord, _zCoord);
		//TileEntity tile = ModLoader.getMinecraftServerInstance().getWorldManager(par1).getBlockTileEntity(_xCoord, _yCoord, _zCoord);
		if (!(tile instanceof TileGenericPipe)) return null;
		TileGenericPipe pipe = (TileGenericPipe) tile;
		if (!(pipe.pipe instanceof CoreRoutedPipe)) return null;
		return (CoreRoutedPipe) pipe.pipe;
	}

		
	private void ensureRouteTableIsUpToDate(){
		if (_LSDVersion > _lastLSDVersion){
			CreateRouteTable();
			_routersByCost  = null;
			_externalRoutersByCost = null;
			_lastLSDVersion = _LSDVersion;
			
			CoreRoutedPipe pipe = getPipe();
			if (pipe == null) return;
			PipeTransportLogistics trans = (PipeTransportLogistics)pipe.transport;
			//HashMap<UUID, Orientations> table = new HashMap<UUID, Orientations>();
		}
	}

	@Override
	public HashMap<Router, Orientations> getRouteTable(){
		ensureRouteTableIsUpToDate();
		return _routeTable;
	}
	
	@Deprecated
	@Override
	public LinkedList<Router> getRoutersByCost(){
		ensureRouteTableIsUpToDate();
		if (_routersByCost == null){
			_routersByCost = new LinkedList<Router>();
			
			LinkedList<RouterCost> tempList = new LinkedList<RouterCost>();
			outer:
			for (Router r : _routeCosts.keySet()){
				for (int i = 0; i < tempList.size(); i++){
					if (_routeCosts.get(r) < tempList.get(i).cost){
						tempList.add(i, new RouterCost(r, _routeCosts.get(r)));
						continue outer;
					}
				}
				tempList.addLast(new RouterCost(r, _routeCosts.get(r)));
			}
			
			while(tempList.size() > 0){
				_routersByCost.addLast(tempList.removeFirst().router);
			}
		}
		return _routersByCost;
	}
	
	@Override
	public LinkedList<IRouter> getIRoutersByCost() {
		ensureRouteTableIsUpToDate();
		if (_externalRoutersByCost  == null){
			_externalRoutersByCost  = new LinkedList<IRouter>();
			
			LinkedList<RouterCost> tempList = new LinkedList<RouterCost>();
			outer:
			for (Router r : _routeCosts.keySet()){
				for (int i = 0; i < tempList.size(); i++){
					if (_routeCosts.get(r) < tempList.get(i).cost){
						tempList.add(i, new RouterCost(r, _routeCosts.get(r)));
						continue outer;
					}
				}
				tempList.addLast(new RouterCost(r, _routeCosts.get(r)));
			}
			
			while(tempList.size() > 0){
				_externalRoutersByCost.addLast(tempList.removeFirst().router);
			}
			_externalRoutersByCost.addFirst(this);
		}		
		return _externalRoutersByCost;
	}
	
	public UUID getId() {
		return this.id;
	}
	

	/**
	 * Rechecks the piped connection to all adjacent routers as well as discover new ones.
	 */
	private void recheckAdjacent()	{
		boolean adjacentChanged = false;
		CoreRoutedPipe thisPipe = getPipe();
		if (thisPipe == null) return;
		HashMap<RoutedPipe, ExitRoute> adjacent = PathFinder.getConnectedRoutingPipes(thisPipe.container, core_LogisticsPipes.LOGISTICS_DETECTION_COUNT, core_LogisticsPipes.LOGISTICS_DETECTION_LENGTH);
		
		for (RoutedPipe pipe : _adjacent.keySet()){
			if(!adjacent.containsKey(pipe))
				adjacentChanged = true;
		}
		
		for (RoutedPipe pipe : adjacent.keySet())	{
			if (!_adjacent.containsKey(pipe)){
				adjacentChanged = true;
				break;
			}
			ExitRoute newExit = adjacent.get(pipe);
			ExitRoute oldExit = _adjacent.get(pipe);
			
			if (newExit.exitOrientation != oldExit.exitOrientation || newExit.metric != oldExit.metric)	{
				adjacentChanged = true;
				break;
			}
		}
		
		if (adjacentChanged){
			_adjacent = adjacent;
			_blockNeedsUpdate = true;
			SendNewLSA();
		}
	}
	
	private void SendNewLSA()
	{
		_myLsa.neighboursWithMetric = new HashMap<Router, Integer>();
		for (RoutedPipe adjacent : _adjacent.keySet()){
			_myLsa.neighboursWithMetric.put(RouterManager.get(adjacent.getRouter().getId()), _adjacent.get(adjacent).metric);
		}
		_LSDVersion++;
		CreateRouteTable();
	}
	
	/**
	 * Create a route table from the link state database
	 */
	private void CreateRouteTable()	{ 
		//Dijkstra!
		
		/** Map of all "approved" routers and the route to get there **/
		HashMap<Router, LinkedList<Router>> tree =  new HashMap<Router, LinkedList<Router>>();
		/** The cost to get to an "approved" router **/
		HashMap<Router, Integer> treeCost = new HashMap<Router, Integer>();
		
		//Init root(er - lol)
		tree.put(this,  new LinkedList<Router>());
		treeCost.put(this,  0);
		/** The candidate router and which approved router put it in the candidate list **/
		HashMap<Router, Router> candidates = new HashMap<Router, Router>();
		/** The total cost for the candidate route **/
		HashMap<Router, Integer> candidatesCost = new HashMap<Router, Integer>();
		
		//Init candidates
		for (RoutedPipe pipe :  _adjacent.keySet()){
			candidates.put(RouterManager.get(pipe.getRouter().getId()), this);
			candidatesCost.put(RouterManager.get(pipe.getRouter().getId()), _adjacent.get(pipe).metric);
		}

		
		while (!candidates.isEmpty()){
			Router lowestCostCandidateRouter = null;
			int lowestCost = Integer.MAX_VALUE;
			for(Router candidate : candidatesCost.keySet()){
				if (candidatesCost.get(candidate) < lowestCost){
					lowestCostCandidateRouter = candidate;
					lowestCost = candidatesCost.get(candidate);
				}
			}
			
			Router lowestParent = candidates.get(lowestCostCandidateRouter);	//Get the approved parent of the lowest cost candidate
			LinkedList<Router> lowestPath = (LinkedList<Router>) tree.get(lowestParent).clone();	//Get a copy of the route for the approved router 
			lowestPath.addLast(lowestCostCandidateRouter); //Add to the route to get to the candidate
			
			//Approve the candidate
			tree.put(lowestCostCandidateRouter, lowestPath);
			treeCost.put(lowestCostCandidateRouter, lowestCost);
			
			//Remove from candidate list
			candidates.remove(lowestCostCandidateRouter);
			candidatesCost.remove(lowestCostCandidateRouter);
			
			//Add new candidates from the newly approved route
			for (LSA lsa : this.SharedLSADatabase){
				if (lsa.source != lowestCostCandidateRouter) continue;				
				for (Router newCandidate: lsa.neighboursWithMetric.keySet()){
					if (tree.containsKey(newCandidate)) continue;
					int candidateCost = lowestCost + lsa.neighboursWithMetric.get(newCandidate);
					if (candidates.containsKey(newCandidate) && candidatesCost.get(newCandidate) <= candidateCost){
						continue;
					}
					candidates.put(newCandidate, lowestCostCandidateRouter);
					candidatesCost.put(newCandidate, candidateCost);
				}
			}
		}
		
		
		//Build route table
		_routeTable = new HashMap<Router, Orientations>();
		_routeCosts = new HashMap<Router, Integer>();
		for (Router node : tree.keySet())
		{
			LinkedList<Router> route = tree.get(node);
			if (route.size() == 0){
				_routeTable.put(node, Orientations.Unknown);
				continue;
			}
			
			Router firstHop = route.getFirst();
			if (firstHop == null) continue;
			CoreRoutedPipe firstPipe = firstHop.getPipe();
			if (firstPipe == null) continue;
			//if (!_adjacent.containsKey(firstHop.getPipe())){
				//System.out.println("FirstHop is not adjacent!");
			//}
			
			if (!_adjacent.containsKey(firstPipe) || _adjacent.get(firstPipe) == null){
				continue;
			}
			
			_routeCosts.put(node, treeCost.get(node));
			_routeTable.put(node, _adjacent.get(firstPipe).exitOrientation);
		}
	}

	public LinkedList<Orientations> GetNonRoutedExits()	{
		LinkedList<Orientations> ret = new LinkedList<Orientations>();
		
		outer:
		for (int i = 0 ; i < 6; i++){
			Orientations o = Orientations.values()[i];
			boolean found = false;
			for(ExitRoute route : _adjacent.values()) {
				if (route.exitOrientation == o){
					found = true;
					continue outer; //Its routed
				}
			}
			ret.add(o);	//Its not (might not be a pipe, but its not routed)
		}
		return ret;
	}
	
	public void displayRoutes(){
		_laser.displayRoute(this);
	}
	
	public void displayRouteTo(IRouter r){
		_laser.displayRoute(this, r);
	}

	@Override
	public int getInboundItemsCount(){
		return _inboundItems.size();
	}

	@Override
	public int getOutboundItemsCount(){
		return _outboundItems.size();
	}
	
	public void startTrackingRoutedItem(RoutedEntityItem routedEntityItem){
		if(!_outboundItems.contains(routedEntityItem)){
			_outboundItems.add(routedEntityItem);
		}
	}

	public void startTrackingInboundItem(RoutedEntityItem routedEntityItem){
		if (!_inboundItems.contains(routedEntityItem)){
			_inboundItems.add(routedEntityItem);
		}
	}
	
	public void outboundItemArrived(RoutedEntityItem routedEntityItem){
		if (_outboundItems.contains(routedEntityItem)){
			_outboundItems.remove(routedEntityItem);
		}
	}
	
	public void inboundItemArrived(RoutedEntityItem routedEntityItem){
		if (_inboundItems.contains(routedEntityItem)){
			_inboundItems.remove(routedEntityItem);
		}

		//notify that Item has arrived
		CoreRoutedPipe pipe = getPipe();	
		if (pipe != null && pipe.logic instanceof IRequireReliableTransport){
			((IRequireReliableTransport)pipe.logic).itemArrived(ItemIdentifier.get(routedEntityItem.item));
		}
	}

	public void itemDropped(RoutedEntityItem routedEntityItem) {
		if (_outboundItems.contains(routedEntityItem)){
			_outboundItems.remove(routedEntityItem);
		}
		if (_inboundItems.contains(routedEntityItem)){
			_inboundItems.remove(routedEntityItem);
		}
	}

	
	/**
		 * Flags the last sent LSA as expired. Each router will be responsible of purging it from its database.
		 */
		@Override
		public void destroy() {
			if (SharedLSADatabase.contains(_myLsa)){
				SharedLSADatabase.remove(_myLsa);
				_LSDVersion++;
			}
			RouterManager.removeRouter(this.id);
		}

	@Override
	public void update(boolean doFullRefresh){
		if (doFullRefresh) {
			recheckAdjacent();
			if (_blockNeedsUpdate){
				CoreRoutedPipe pipe = getPipe();
				if (pipe == null) return;
				pipe.worldObj.markBlockAsNeedsUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);	
				_blockNeedsUpdate = false;
			}
			return;
		}
	}

	/************* IROUTER *******************/
	
	
	@Override
	public void sendRoutedItem(ItemStack item, Router destination, Position origin) {
		// TODO Auto-generated method stub
		
	}

	public boolean isRoutedExit(Orientations o){
		return !GetNonRoutedExits().contains(o);
	}

	@Override
	public Orientations getExitFor(UUID id) {
		return this.getRouteTable().get(RouterManager.get(id));
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
}


