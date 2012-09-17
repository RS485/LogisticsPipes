package logisticspipes.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import cpw.mods.fml.common.network.PacketDispatcher;

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
import buildcraft.core.network.PacketCoordinates;
import buildcraft.transport.TileGenericPipe;

public class ClientRouter implements IRouter {

	public UUID id;
	private final int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	public boolean[] routedExit = new boolean[6];

	private HashMap<UUID, Orientations> _routeTable = new HashMap<UUID, Orientations>();
	private HashMap<UUID, Integer> _routeCosts = new HashMap<UUID, Integer>();
	private LinkedList<IRouter> _externalRoutersByCost = null;
	
	public ClientRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		this.id = id;
		this._dimension = dimension;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
		PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.REQUEST_ROUTER_UPDATE, _xCoord, _yCoord, _zCoord, _dimension).getPacket());
	}

	@Override
	public void destroy() {
		//Not On Client Side
	}

	@Override
	public void update(boolean fullRefresh) {
		//Not On Client Side
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
		return this.getRouteTable().get(SimpleServiceLocator.routerManager.getRouter(id));
	}

	@Override
	public HashMap<IRouter, Orientations> getRouteTable() {
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
		this._routeCosts = packet._routeCosts;
		if(_routeCosts == null) {
			_routeCosts = new HashMap<UUID, Integer>();
		}
		this._routeTable = packet._routeTable;
		if(_routeTable == null) {
			_routeTable = new HashMap<UUID, Orientations>();
		}
		_externalRoutersByCost = null;
		this.id = packet.uuid;
		this.routedExit = packet.routedExit;
		CoreRoutedPipe pipe = getPipe();
		if(pipe != null) {
			pipe.refreshRouterIdFromRouter();
		}
	}
}
