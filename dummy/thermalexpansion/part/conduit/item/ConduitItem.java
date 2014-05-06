package thermalexpansion.part.conduit.item;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thermalexpansion.part.conduit.ConduitBase;
import thermalexpansion.part.conduit.Grid;

public class ConduitItem extends ConduitBase {
	public List<TravelingItem>	myItems;
	public static final routeInfo noRoute = new routeInfo();
    public LinkedList<ItemRoute> validOutputs;
	public ConduitItem(byte b) {}
	public boolean isItemConduit() {return true;}
	public ConduitItem getConduitItem() {return this;}
	public ConduitBase getConduit(int side) {return null;}
	public boolean isConduit(TileEntity curTile) {return false;}
	public boolean isImportant(TileEntity curTile, int side) {return false;}
	public void cacheImportant(TileEntity theTile, int side) {}
	public routeInfo canRouteItem(ItemStack anItem, boolean isSelf, int maxTransferSize) {return null;}
	public ItemStack insertItem(ForgeDirection from, ItemStack item) {return null;}
	public void insertItem(TravelingItem travelingItem) {}
	public static class routeInfo {
		public routeInfo(int stackSizeLeft, byte i) {
			this.canRoute = true;
			this.stackSize = stackSizeLeft;
			this.side = i;
		}
		public boolean	canRoute	= false;
		public int		stackSize	= -1;
		public byte		side		= -1;
		public routeInfo() {}
	}
	public int x() {return 0;}
	public int y() {return 0;}
	public int z() {return 0;}
	public World world() {return null;}
	public int getLength() {return 0;}
	public void cacheRoutes() {}
	public int doRouteItem(ItemRoute aRoute, routeInfo curInfo, ItemStack theItem, int inventorySide) {return 0;}
	public void setGrid(Grid newGrid) {}
}
