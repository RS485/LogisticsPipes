package logisticspipes.proxy.buildcraft;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.fluid.LogisticsFluidConnectorPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeStructureCobblestone;

public class BCPipeInformationProvider implements IPipeInformationProvider {
	
	private final TileGenericPipe pipe;
	public BCPipeInformationProvider(TileGenericPipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public boolean isCorrect() {
		return true;
	}
	
	@Override
	public int getX() {
		return pipe.xCoord;
	}
	
	@Override
	public int getY() {
		return pipe.yCoord;
	}
	
	@Override
	public int getZ() {
		return pipe.zCoord;
	}
	
	@Override
	public World getWorld() {
		return pipe.getWorld();
	}
	
	@Override
	public boolean isInitialised() {
		return pipe.initialized;
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
		return pipe.getTile(direction);
	}
	
	@Override
	public boolean isFirewallPipe() {
		return false;
	}
	
	@Override
	public IFilter getFirewallFilter() {
		throw new RuntimeException("This is not a firewall pipe");
	}
	
	@Override
	public TileEntity getTile() {
		return pipe;
	}
	
	@Override
	public boolean divideNetwork() {
		if (pipe.pipe instanceof LogisticsFluidConnectorPipe) {	
			return true;
		}
		if (pipe.pipe instanceof PipeItemsObsidian) {			//Obsidian seperates networks
			return true;
		}
		if (pipe.pipe instanceof PipeStructureCobblestone) {	//don't recurse onto structure pipes.
			return true;
		}
		return false;
	}
	
	@Override
	public boolean powerOnly() {
		if(pipe.pipe instanceof PipeItemsDiamond) {		//Diamond only allows power through
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isOnewayPipe() {
		if(pipe.pipe instanceof PipeItemsIron){	//Iron requests and power can come from closed sides
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isOutputOpen(ForgeDirection direction) {
		return pipe.pipe.outputOpen(direction);
	}

	@Override
	public boolean canConnect(IPipeInformationProvider provider, ForgeDirection direction, boolean flag) {
		return SimpleServiceLocator.buildCraftProxy.checkPipesConnections(pipe, provider.getTile(), direction, true);
	}

	@Override
	public int getDistance() {
		return 1;
	}
}
