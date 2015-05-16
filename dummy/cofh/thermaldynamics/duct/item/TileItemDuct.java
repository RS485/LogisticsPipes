package cofh.thermaldynamics.duct.item;

import net.minecraft.item.ItemStack;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.IMultiBlockRoute;
import cofh.thermaldynamics.multiblock.RouteCache;

public class TileItemDuct extends TileTDBase implements IMultiBlockRoute {
	  public ItemGrid internalGrid;
	  public static final RouteInfo noRoute = new RouteInfo();
	  public static class RouteInfo
	  {
	    public RouteInfo(int paramInt, byte paramByte)
	    {
	      this.canRoute = true;
	      this.stackSize = paramInt;
	      this.side = paramByte;
	    }
	    
	    public boolean canRoute = false;
	    public int stackSize = -1;
	    public byte side = -1;
	    
	    public RouteInfo() {}
	  }

	public RouteInfo canRouteItem(ItemStack arg0) {
		throw new UnsupportedOperationException();
	}

	public void transferItem(TravelingItem arg0) {
		throw new UnsupportedOperationException();
	}

	public void insertNewItem(TravelingItem paramTravelingItem) {
		throw new UnsupportedOperationException();
	}
	
	public RouteCache getCache(boolean paramBoolean) {
		throw new UnsupportedOperationException();
	}
	
	public TileTDBase.NeighborTypes getCachedSideType(byte paramByte) {
		throw new UnsupportedOperationException();
	}
	
	public TileTDBase.ConnectionTypes getConnectionType(byte paramByte) {
		throw new UnsupportedOperationException();
	}
	
	public IMultiBlock getCachedTile(byte paramByte) {
		throw new UnsupportedOperationException();
	}
}
