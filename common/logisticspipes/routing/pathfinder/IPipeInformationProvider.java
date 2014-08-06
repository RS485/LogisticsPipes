package logisticspipes.routing.pathfinder;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IPipeInformationProvider {
	public boolean isCorrect();
	public int getX();
	public int getY();
	public int getZ();
	public World getWorld();
	public boolean isInitialised();
	public boolean isRoutingPipe();
	public CoreRoutedPipe getRoutingPipe();
	public TileEntity getTile(ForgeDirection direction);
	public boolean isFirewallPipe();
	public IFilter getFirewallFilter();
	public TileEntity getTile();
	public boolean divideNetwork();
	public boolean powerOnly();
	public boolean isOnewayPipe();
	public boolean isOutputOpen(ForgeDirection direction);
	public boolean canConnect(TileEntity to, ForgeDirection direction, boolean flag);
	public int getDistance();
	public boolean isItemPipe();
	public boolean isFluidPipe();
	public boolean isPowerPipe();
}
