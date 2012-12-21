package logisticspipes.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class ClientRouter implements IRouter {
	
	public UUID id;
	private final int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	public boolean[] routedExit = new boolean[6];
	
	public ClientRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		this.id = id;
		this._dimension = dimension;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
	}

	@Override
	public void destroy() {
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
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return false;
	}

	@Override
	public boolean hasRoute(UUID id) {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return false;
	}

	@Override
	public ForgeDirection getExitFor(UUID id) {
		return this.getRouteTable().get(SimpleServiceLocator.routerManager.getRouter(id));
	}

	@Override
	public HashMap<IRouter, ForgeDirection> getRouteTable() {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new HashMap<IRouter, ForgeDirection>();
	}

	@Override
	public LinkedList<IRouter> getIRoutersByCost() {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new LinkedList<IRouter>();
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
}
