package logisticspipes.proxy.te;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thermalexpansion.part.conduit.IConduit;
import thermalexpansion.part.conduit.item.ConduitItem;

public class TEPipeInformationProvider implements IPipeInformationProvider {
	
	private ConduitItem conduit;
	
	public TEPipeInformationProvider(IConduit con) {
		if(con.isItemConduit()) {
			conduit = con.getConduitItem();
		}
	}
	
	@Override
	public boolean isCorrect() {
		return conduit != null;
	}
	
	@Override
	public int getX() {
		return conduit.x();
	}
	
	@Override
	public int getY() {
		return conduit.y();
	}
	
	@Override
	public int getZ() {
		return conduit.z();
	}
	
	@Override
	public World getWorld() {
		return conduit.world();
	}
	
	@Override
	public boolean isInitialised() { //TODO what needs to be checked ?
		return true;
	}
	
	@Override
	public boolean isRoutingPipe() {
		return false;
	}
	
	@Override
	public CoreRoutedPipe getRoutingPipe() {
		throw new RuntimeException("This is no routing pipe");
	}
	
	@Override
	public TileEntity getTile(ForgeDirection direction) {
		LPPosition p = new LPPosition(this);
		p.moveForward(direction);
		return p.getTileEntity(conduit.world());
	}
	
	@Override
	public boolean isFirewallPipe() {
		return false;
	}
	
	@Override
	public IFilter getFirewallFilter() {
		throw new RuntimeException("This is no firewall pipe");
	}
	
	@Override
	public TileEntity getTile() {
		return conduit.tile();
	}
	
	@Override
	public boolean divideNetwork() {
		return false;
	}
	
	@Override
	public boolean powerOnly() {
		return false;
	}
	
	@Override
	public boolean isOnewayPipe() {
		return false;
	}
	
	@Override
	public boolean isOutputOpen(ForgeDirection direction) {
		return false;
	}
	
	@Override
	public boolean canConnect(TileEntity tile, ForgeDirection direction, boolean flag) {
		return conduit.sideType[direction.ordinal()] != 0;
	}

	@Override
	public int getDistance() {
		return conduit.getLength();
	}

	@Override
	public boolean isItemPipe() {
		return true;
	}

	@Override
	public boolean isFluidPipe() {
		return false;
	}

	@Override
	public boolean isPowerPipe() {
		return false;
	}	
}
