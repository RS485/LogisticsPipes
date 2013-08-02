package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.TileGenericPipe;

public class TesseractConnection implements ISpecialTileConnection {
	
	@Override
	public boolean init() {
		return SimpleServiceLocator.thermalExpansionProxy.isTE();
	}

	@Override
	public boolean isType(TileEntity tile) {
		return SimpleServiceLocator.thermalExpansionProxy.isTesseract(tile);
	}

	@Override
	public List<TileGenericPipe> getConnections(TileEntity tile) {
		boolean onlyOnePipe = false;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			Position p = new Position(tile.xCoord, tile.yCoord, tile.zCoord, direction);
			p.moveForwards(1);
			TileEntity canidate = tile.getWorldObj().getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			if(canidate instanceof TileGenericPipe && SimpleServiceLocator.buildCraftProxy.checkPipesConnections(tile, canidate, direction)) {
				if(onlyOnePipe) {
					onlyOnePipe = false;
					break;
				} else {
					onlyOnePipe = true;
				}
			}
		}
		if(!onlyOnePipe) {
			return new ArrayList<TileGenericPipe>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.thermalExpansionProxy.getConnectedTesseracts(tile);
		List<TileGenericPipe> list = new ArrayList<TileGenericPipe>();
		for(TileEntity connected:connections) {
			TileGenericPipe pipe = null;
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				Position p = new Position(connected.xCoord, connected.yCoord, connected.zCoord, direction);
				p.moveForwards(1);
				TileEntity canidate = connected.getWorldObj().getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
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
			return new ArrayList<TileGenericPipe>(0);
		}
	}

	@Override
	public boolean needsInformationTransition() {
		return true;
	}

	@Override
	public void transmit(TileEntity tile, TravelingItem data) {
		List<TileGenericPipe> list = getConnections(tile);
		if(list.size() < 1) return;
		TileGenericPipe pipe = list.get(0);
		((CoreRoutedPipe)pipe.pipe).queueUnroutedItemInformation(data);
	}
}
