package logisticspipes.routing.pathfinder;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.List;



public interface IPipeInformationProvider {

	public boolean isCorrect();

	public int getX();

	public int getY();

	public int getZ();

	public World getWorld();

	public boolean isRouterInitialized();

	public boolean isRoutingPipe();

	public CoreRoutedPipe getRoutingPipe();

	public TileEntity getTile(EnumFacing direction);

	public boolean isFirewallPipe();

	public IFilter getFirewallFilter();

	public TileEntity getTile();

	public boolean divideNetwork();

	public boolean powerOnly();

	public boolean isOnewayPipe();

	public boolean isOutputOpen(EnumFacing direction);

	public boolean canConnect(TileEntity to, EnumFacing direction, boolean flag);

	public double getDistance();

	public boolean isItemPipe();

	public boolean isFluidPipe();

	public boolean isPowerPipe();

	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double travled, double max, List<LPPosition> visited);

	public boolean acceptItem(LPTravelingItem item, TileEntity from);

	public void refreshTileCacheOnSide(EnumFacing side);
}
