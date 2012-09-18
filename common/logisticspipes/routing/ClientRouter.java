package logisticspipes.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.main.CoreRoutedPipe;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketRouterInformation;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.PacketDispatcher;

public class ClientRouter implements IRouter {

	private class LSA {
		public IRouter source;
		public HashMap<UUID, Integer> neighboursWithMetric;
	}
	
	public UUID id;
	private final int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	public boolean[] routedExit = new boolean[6];

	public HashMap<UUID, ExitRoute> _adjacent = new HashMap<UUID, ExitRoute>();
	
	private HashMap<UUID, Orientations> _routeTable = new HashMap<UUID, Orientations>();
	private HashMap<UUID, Integer> _routeCosts = new HashMap<UUID, Integer>();
	private LinkedList<IRouter> _externalRoutersByCost = null;
	
	private static int _LSDVersion = 0;
	private int _lastLSDVersion = 0;
	
	private static LinkedList<LSA> SharedLSADatabase = new LinkedList<LSA>();
	private LSA _myLsa = new LSA();
	
	
	public ClientRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		this.id = id;
		this._dimension = dimension;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
		PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.REQUEST_ROUTER_UPDATE, _xCoord, _yCoord, _zCoord, _dimension).getPacket());
		_myLsa = new LSA();
		_myLsa.source = this;
		_myLsa.neighboursWithMetric = new HashMap<UUID, Integer>();
		SharedLSADatabase.add(_myLsa);
	}

	@Override
	public void destroy() {
		if (SharedLSADatabase.contains(_myLsa)){
			SharedLSADatabase.remove(_myLsa);
			_LSDVersion++;
		}
		SimpleServiceLocator.routerManager.removeRouter(this.id);
	}

	@Override
	public void update(boolean fullRefresh) {
		
	}

	@Override
	public void sendRoutedItem(ItemStack item, IRouter destination, Position origin) {
		//Not On Client Side
	}

	@Override
	public boolean isRoutedExit(Orientations connection) {
		if(connection == Orientations.Unknown) {
			return false;
		}
		return routedExit[connection.ordinal()];
	}

	@Override
	public boolean hasRoute(UUID id) {
		if (!SimpleServiceLocator.routerManager.isRouter(id)) return false;
		
		IRouter r = SimpleServiceLocator.routerManager.getRouter(id);
		
		if (!this.getRouteTable().containsKey(r)) return false;
		
		return true;
	}

	@Override
	public Orientations getExitFor(UUID id) {
		ensureRouteTableIsUpToDate();
		return this.getRouteTable().get(SimpleServiceLocator.routerManager.getRouter(id));
	}

	@Override
	public HashMap<IRouter, Orientations> getRouteTable() {
		ensureRouteTableIsUpToDate();
		HashMap<IRouter, Orientations> list = new HashMap<IRouter, Orientations>();
		for(UUID id: _routeTable.keySet()) {
			Orientations ori = _routeTable.get(id);
			IRouter router =  SimpleServiceLocator.routerManager.getRouter(id);
			if(router != null) {
				list.put(router, ori);
			}
		}
		return list;
	}

	@Override
	public LinkedList<IRouter> getIRoutersByCost() {
		ensureRouteTableIsUpToDate();
		if (_externalRoutersByCost == null){
			_externalRoutersByCost = new LinkedList<IRouter>();
			
			LinkedList<RouterCost> tempList = new LinkedList<RouterCost>();
			outer:
			for (UUID id : _routeCosts.keySet()){
				IRouter r = SimpleServiceLocator.routerManager.getRouter(id);
				if(r == null) continue;
				for (int i = 0; i < tempList.size(); i++){
					if (_routeCosts.get(id) < tempList.get(i).cost){
						tempList.add(i, new RouterCost(r, _routeCosts.get(id)));
						continue outer;
					}
				}
				tempList.addLast(new RouterCost(r, _routeCosts.get(id)));
			}
			
			while(tempList.size() > 0){
				_externalRoutersByCost.addLast(tempList.removeFirst().router);
			}
			_externalRoutersByCost.addFirst(this);
		}
		return _externalRoutersByCost;
	}
	
	/**
	 * Create a route table from the link state database
	 */
	private void CreateRouteTable()	{ 
		//Dijkstra!
		
		/** Map of all "approved" routers and the route to get there **/
		HashMap<IRouter, LinkedList<IRouter>> tree =  new HashMap<IRouter, LinkedList<IRouter>>();
		/** The cost to get to an "approved" router **/
		HashMap<IRouter, Integer> treeCost = new HashMap<IRouter, Integer>();
		
		//Init root(er - lol)
		tree.put(this,  new LinkedList<IRouter>());
		treeCost.put(this,  0);
		/** The candidate router and which approved router put it in the candidate list **/
		HashMap<IRouter, IRouter> candidates = new HashMap<IRouter, IRouter>();
		/** The total cost for the candidate route **/
		HashMap<IRouter, Integer> candidatesCost = new HashMap<IRouter, Integer>();
		
		//Init candidates
		for (UUID pipeId :  _adjacent.keySet()){
			candidates.put(SimpleServiceLocator.routerManager.getRouter(pipeId), this);
			candidatesCost.put(SimpleServiceLocator.routerManager.getRouter(pipeId), _adjacent.get(pipeId).metric);
		}

		
		while (!candidates.isEmpty()){
			IRouter lowestCostCandidateRouter = null;
			int lowestCost = Integer.MAX_VALUE;
			for(IRouter candidate : candidatesCost.keySet()){
				if (candidatesCost.get(candidate) < lowestCost){
					lowestCostCandidateRouter = candidate;
					lowestCost = candidatesCost.get(candidate);
				}
			}
			
			IRouter lowestParent = candidates.get(lowestCostCandidateRouter);	//Get the approved parent of the lowest cost candidate
			LinkedList<IRouter> lowestPath = (LinkedList<IRouter>) tree.get(lowestParent).clone();	//Get a copy of the route for the approved router 
			lowestPath.addLast(lowestCostCandidateRouter); //Add to the route to get to the candidate
			
			//Approve the candidate
			tree.put(lowestCostCandidateRouter, lowestPath);
			treeCost.put(lowestCostCandidateRouter, lowestCost);
			
			//Remove from candidate list
			candidates.remove(lowestCostCandidateRouter);
			candidatesCost.remove(lowestCostCandidateRouter);
			
			//Add new candidates from the newly approved route
			for (LSA lsa : SharedLSADatabase){
				if (lsa.source != lowestCostCandidateRouter) continue;				
				for (UUID newCandidate: lsa.neighboursWithMetric.keySet()){
					if (tree.containsKey(SimpleServiceLocator.routerManager.getRouter(newCandidate))) continue;
					int candidateCost = lowestCost + lsa.neighboursWithMetric.get(newCandidate);
					if (candidates.containsKey(SimpleServiceLocator.routerManager.getRouter(newCandidate)) && candidatesCost.get(SimpleServiceLocator.routerManager.getRouter(newCandidate)) <= candidateCost){
						continue;
					}
					candidates.put(SimpleServiceLocator.routerManager.getRouter(newCandidate), lowestCostCandidateRouter);
					candidatesCost.put(SimpleServiceLocator.routerManager.getRouter(newCandidate), candidateCost);
				}
			}
		}
		
		
		//Build route table
		_routeTable = new HashMap<UUID, Orientations>();
		_routeCosts = new HashMap<UUID, Integer>();
		for (IRouter node : tree.keySet())
		{
			LinkedList<IRouter> route = tree.get(node);
			if (route.size() == 0){
				_routeTable.put(node.getId(), Orientations.Unknown);
				continue;
			}
			
			IRouter firstHop = route.getFirst();
			if (firstHop == null) continue;
			//if (!_adjacent.containsKey(firstHop.getPipe())){
				//System.out.println("FirstHop is not adjacent!");
			//}
			
			if (!_adjacent.containsKey(firstHop.getId()) || _adjacent.get(firstHop.getId()) == null){
				continue;
			}
			
			_routeCosts.put(node.getId(), treeCost.get(node));
			_routeTable.put(node.getId(), _adjacent.get(firstHop.getId()).exitOrientation);
		}
	}

	private void ensureRouteTableIsUpToDate(){
		if (_LSDVersion > _lastLSDVersion){
			CreateRouteTable();
			_externalRoutersByCost = null;
			_lastLSDVersion = _LSDVersion;
		}
	}

	@Override
	public CoreRoutedPipe getPipe() {
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

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void itemDropped(RoutedEntityItem routedEntityItem) {
		//Not On Client Side
	}

	@Override
	public void displayRoutes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayRouteTo(IRouter r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inboundItemArrived(RoutedEntityItem routedEntityItem) {
		//Not On Client Side
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		CoreRoutedPipe pipe = this.getPipe();
		if (pipe == null) return null;
		return pipe.getLogisticsModule();
	}
	
	public void handleRouterPacket(PacketRouterInformation packet) {
		this._adjacent = packet._adjacent;
		if(_adjacent == null) {
			_adjacent = new HashMap<UUID, ExitRoute>();
		}
		_myLsa.neighboursWithMetric = new HashMap<UUID, Integer>();
		for (UUID adjacent : _adjacent.keySet()){
			_myLsa.neighboursWithMetric.put(adjacent, _adjacent.get(adjacent).metric);
		}
		_LSDVersion++;
		this.id = packet.uuid;
		this.routedExit = packet.routedExit;
		CoreRoutedPipe pipe = getPipe();
		if(pipe != null) {
			pipe.refreshRouterIdFromRouter();
		}
	}
}
