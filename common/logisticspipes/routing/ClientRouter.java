package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class ClientRouter implements IRouter {

	private static int firstFreeId = 0;
	private static BitSet simpleIdUsedSet = new BitSet();

	private static int claimSimpleID(int id2) {
		if(id2>=0 && !simpleIdUsedSet.get(id2)){
			simpleIdUsedSet.set(id2);
			return id2;
		}
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
	
	public final UUID globalId;
	public final int id;
	private final int _dimension;
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	public boolean[] routedExit = new boolean[6];
	
	public ClientRouter(UUID id, int simpleId, int dimension, int xCoord, int yCoord, int zCoord) {
		if(id != null) {
			globalId=id;
		} else {
			globalId =UUID.randomUUID();
		}
		this.id = claimSimpleID(simpleId);
		this._dimension = dimension;
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
	}

	@Override
	public void destroy() {
		SimpleServiceLocator.routerManager.removeRouter(this.getSimpleID());
	}

	@Override
	public int getSimpleID() {
		return id;
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
	public boolean hasRoute(int id) {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return false;
	}

	@Override
	public ForgeDirection getExitFor(int id) {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return ForgeDirection.UNKNOWN;
	}

	@Override
	public HashMap<IRouter, Pair<ForgeDirection, ForgeDirection>> getRouteTable() {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new HashMap<IRouter, Pair<ForgeDirection,ForgeDirection>>();
	}

	@Override
	public List<SearchNode> getIRoutersByCost() {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new LinkedList<SearchNode>();
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
	
	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord){
		return _dimension == dimension && _xCoord == xCoord && _yCoord == yCoord && _zCoord == zCoord;
	}


	@Override
	public UUID getId() {
		return globalId;
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

	@Override
	public void clearPipeCache() {
		//Not On Client Side		
	}

	@Override
	public List<ILogisticsPowerProvider> getPowerProvider() {
		return null;
	}

	@Override
	public List<ILogisticsPowerProvider> getConnectedPowerProvider() {
		return null;
	}
}
