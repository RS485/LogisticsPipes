package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;

public class ClientRouter implements IRouter {
	private final int _xCoord;
	private final int _yCoord;
	private final int _zCoord;
	
	public ClientRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		this._xCoord = xCoord;
		this._yCoord = yCoord;
		this._zCoord = zCoord;
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getSimpleID() {
		return -420;
	}

	@Override
	public void update(boolean fullRefresh) {
		
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
	public ArrayList<ExitRoute> getRouteTable() {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new  ArrayList<ExitRoute>();
	}

	@Override
	public List<ExitRoute> getIRoutersByCost() {
		if(LogisticsPipes.DEBUG) {
			throw new UnsupportedOperationException("noClientRouting");
		}
		return new LinkedList<ExitRoute>();
	}

	@Override
	public CoreRoutedPipe getPipe() {
		World worldObj = MainProxy.proxy.getWorld();
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
	public CoreRoutedPipe getCachedPipe(){
		return getPipe();
	}

	@Override
	public boolean isInDim(int dimension) {
		return true;
	}

	@Override
	public boolean isAt(int dimension, int xCoord, int yCoord, int zCoord){
		return  _xCoord == xCoord && _yCoord == yCoord && _zCoord == zCoord;
	}


	@Override
	public UUID getId() {
		return  UUID.randomUUID();
	}

	@Override
	public void inboundItemArrived(RoutedEntityItem routedEntityItem) {
		//Not On Client Side
	}

	@Override
	public LogisticsModule getLogisticsModule() {
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
	public IRouter getRouter(ForgeDirection insertOrientation) {
		return null;
	}

	@Override
	public boolean act(BitSet hasBeenProcessed, IRAction actor) {
		return false;
	}

	@Override
	public void flagForRoutingUpdate() {
		
	}

	@Override
	public boolean checkAdjacentUpdate() {
		return false;
	}

	@Override
	public void clearPrevAdjacent() {
		
	}

	@Override
	public boolean isSideDisconneceted(ForgeDirection dir) {
		return false;
	}

	@Override
	public void updateInterests() {
		
	}

	@Override
	public ExitRoute getDistanceTo(IRouter r) {
		return null;
	}

	@Override
	public void clearInterests() {
		
	}

	@Override
	public List<IRouter> getFilteringRouter() {
		return null;
	}

	@Override
	public boolean isValidCache() {
		return true;
	}
}
