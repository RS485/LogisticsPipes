package logisticspipes.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketRouterInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Pair;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.PacketDispatcher;

public class ClientRouter implements IRouter {

	private class LSA {
		public IRouter source;
		public HashMap<UUID, Pair<Integer, Boolean>> neighboursWithMetric;
	}
	
	public UUID id;
	private final int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	public boolean[] routedExit = new boolean[6];
	
	private int failed = 0;

	public HashMap<UUID, ExitRoute> _adjacent = new HashMap<UUID, ExitRoute>();
	
	private HashMap<UUID, ForgeDirection> _routeTable = new HashMap<UUID, ForgeDirection>();
	public HashMap<UUID, Pair<Integer,Boolean>> _routeCosts = new HashMap<UUID, Pair<Integer,Boolean>>();
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
		_myLsa.neighboursWithMetric = new HashMap<UUID, Pair<Integer, Boolean>>();
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
	public boolean isRoutedExit(ForgeDirection connection) {
		if(connection == ForgeDirection.UNKNOWN) {
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
	public ForgeDirection getExitFor(UUID id) {
		ensureRouteTableIsUpToDate();
		return this.getRouteTable().get(SimpleServiceLocator.routerManager.getRouter(id));
	}

	@Override
	public HashMap<IRouter, ForgeDirection> getRouteTable() {
		ensureRouteTableIsUpToDate();
		HashMap<IRouter, ForgeDirection> list = new HashMap<IRouter, ForgeDirection>();
		for(UUID id: _routeTable.keySet()) {
			ForgeDirection ori = _routeTable.get(id);
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
					if (_routeCosts.get(id).getValue1() < tempList.get(i).cost){
						tempList.add(i, new RouterCost(r, _routeCosts.get(id).getValue1()));
						continue outer;
					}
				}
				tempList.addLast(new RouterCost(r, _routeCosts.get(id).getValue1()));
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
		HashMap<IRouter, Pair<Integer, Boolean>> treeCost = new HashMap<IRouter, Pair<Integer, Boolean>>();
		
		//Init root(er - lol)
		tree.put(this,  new LinkedList<IRouter>());
		treeCost.put(this,  new Pair(0, false));
		/** The candidate router and which approved router put it in the candidate list **/
		HashMap<IRouter, IRouter> candidates = new HashMap<IRouter, IRouter>();
		/** The total cost for the candidate route **/
		HashMap<IRouter, Pair<Integer,Boolean>> candidatesCost = new HashMap<IRouter, Pair<Integer,Boolean>>();
		
		//Init candidates
		for (UUID pipeId :  _adjacent.keySet()){
			candidates.put(SimpleServiceLocator.routerManager.getRouter(pipeId), this);
			candidatesCost.put(SimpleServiceLocator.routerManager.getRouter(pipeId), new Pair(_adjacent.get(pipeId).metric,_adjacent.get(pipeId).isPipeLess));
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
			LinkedList<IRouter> lowestPath = (LinkedList<IRouter>) tree.get(lowestParent).clone();	//Get a copy of the route for the approved router 
			lowestPath.addLast(lowestCostCandidateRouter); //Add to the route to get to the candidate
			
			//Approve the candidate
			tree.put(lowestCostCandidateRouter, lowestPath);
			treeCost.put(lowestCostCandidateRouter, new Pair(lowestCost,isPipeLess));
			
			//Remove from candidate list
			candidates.remove(lowestCostCandidateRouter);
			candidatesCost.remove(lowestCostCandidateRouter);
			
			//Add new candidates from the newly approved route
			for (LSA lsa : SharedLSADatabase) {
				if (lsa.source != lowestCostCandidateRouter) continue;				
				for (UUID newCandidate: lsa.neighboursWithMetric.keySet()){
					IRouter router = SimpleServiceLocator.routerManager.getRouter(newCandidate);
					if (tree.containsKey(router)) {
						if(treeCost.get(router).getValue2() && !isPipeLess) {
							treeCost.get(router).setValue2(false);
						}
						continue;
					}
					int candidateCost = lowestCost + lsa.neighboursWithMetric.get(newCandidate).getValue1();
					if (candidates.containsKey(router) && candidatesCost.get(router).getValue1() <= candidateCost){
						continue;
					}
					candidates.put(router, lowestCostCandidateRouter);
					candidatesCost.put(router, new Pair(candidateCost, isPipeLess || lsa.neighboursWithMetric.get(newCandidate).getValue2()));
				}
			}
		}
		
		
		//Build route table
		_routeTable = new HashMap<UUID, ForgeDirection>();
		_routeCosts = new HashMap<UUID, Pair<Integer, Boolean>>();
		for (IRouter node : tree.keySet())
		{
			LinkedList<IRouter> route = tree.get(node);
			if (route.size() == 0){
				_routeTable.put(node.getId(), ForgeDirection.UNKNOWN);
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
			
			if(node == null || node.getId() == null || treeCost == null || treeCost.get(node) == null) {
				System.out.println();
				continue;
			}
			
			_routeCosts.put(node.getId(), treeCost.get(node));
			_routeTable.put(node.getId(), _adjacent.get(firstHop.getId()).exitOrientation);
		}
	}

	private void ensureRouteTableIsUpToDate(){
		if (_LSDVersion > _lastLSDVersion) {
			if(failed > 10) {
				_lastLSDVersion = _LSDVersion;
			} else {
				try {
					CreateRouteTable();
					_externalRoutersByCost = null;
					_lastLSDVersion = _LSDVersion;
					failed = 0;
				} catch(Exception e) {
					if(LogisticsPipes.DEBUG) {
						e.printStackTrace();
					}
					failed++;
				}
			}
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
		if(id != null) {
			return id;
		} else {
			return id = UUID.randomUUID();
		}
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
		_myLsa.neighboursWithMetric = new HashMap<UUID, Pair<Integer, Boolean>>();
		for (UUID adjacent : _adjacent.keySet()){
			_myLsa.neighboursWithMetric.put(adjacent, new Pair(_adjacent.get(adjacent).metric, _adjacent.get(adjacent).isPipeLess));
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
