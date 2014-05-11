package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;

public class EnderIOHyperCubeConnection implements ISpecialTileConnection {
	
	@Override
	public boolean init() {
		return SimpleServiceLocator.enderIOProxy.isEnderIO();
	}

	@Override
	public boolean isType(TileEntity tile) {
		return SimpleServiceLocator.enderIOProxy.isHyperCube(tile);
	}

	@Override
	public List<TileEntity> getConnections(TileEntity tile) {
		boolean onlyOnePipe = false;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			LPPosition p = new LPPosition(tile);
			p.moveForward(direction);
			TileEntity canidate = p.getTileEntity(tile.getWorldObj());
			if(canidate instanceof TileGenericPipe && SimpleServiceLocator.buildCraftProxy.checkPipesConnections(tile, canidate, direction)) {
				if(onlyOnePipe) {
					onlyOnePipe = false;
					break;
				} else {
					onlyOnePipe = true;
				}
			}
		}
		if(!onlyOnePipe || !SimpleServiceLocator.enderIOProxy.isSendAndReceive(tile)) {
			return new ArrayList<TileEntity>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.enderIOProxy.getConnectedHyperCubes(tile);
		List<TileEntity> list = new ArrayList<TileEntity>();
		for(TileEntity connected:connections) {
			if(!SimpleServiceLocator.enderIOProxy.isSendAndReceive(connected)) continue;
			TileGenericPipe pipe = null;
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				LPPosition p = new LPPosition(connected);
				p.moveForward(direction);
				TileEntity canidate = p.getTileEntity(tile.getWorldObj());
				if(canidate instanceof TileGenericPipe && SimpleServiceLocator.buildCraftProxy.checkPipesConnections(connected, canidate, direction)) {
					if(pipe != null) {
						pipe = null;
						break;
					} else {
						pipe = (TileGenericPipe) canidate;
					}
				}
			}
			if(pipe != null && pipe.pipe instanceof CoreRoutedPipe) {
				list.add(pipe);
			}
		}
		if(list.size() == 1) {
			return list;
		} else {
			return new ArrayList<TileEntity>(0);
		}
	}

	@Override
	public boolean needsInformationTransition() {
		return true;
	}

	@Override
	public void transmit(TileEntity tile, IRoutedItem data) {
		List<TileEntity> list = getConnections(tile);
		if(list.size() < 1) return;
		TileEntity pipe = list.get(0);
		if(pipe instanceof TileGenericPipe) {
			((CoreRoutedPipe)((TileGenericPipe)pipe).pipe).queueUnroutedItemInformation(data.getItemIdentifierStack().clone(), data.getInfo());
		} else {
			new RuntimeException("Only LP pipes can be next to Teseracts to queue item informaiton").printStackTrace();
		}
	}
}
