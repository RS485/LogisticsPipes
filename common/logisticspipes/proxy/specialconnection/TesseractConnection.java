package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import buildcraft.api.core.Position;
import buildcraft.transport.TravelingItem;
import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.transport.LPTravelingItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
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
	public List<TileEntity> getConnections(TileEntity tile) {
		boolean onlyOnePipe = false;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			Position p = new Position(tile.xCoord, tile.yCoord, tile.zCoord, direction);
			p.moveForwards(1);
			TileEntity canidate = tile.getWorldObj().getTileEntity((int) p.x, (int) p.y, (int) p.z);
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
			return new ArrayList<TileEntity>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.thermalExpansionProxy.getConnectedTesseracts(tile);
		List<TileEntity> list = new ArrayList<TileEntity>();
		for(TileEntity connected:connections) {
			TileGenericPipe pipe = null;
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				Position p = new Position(connected.xCoord, connected.yCoord, connected.zCoord, direction);
				p.moveForwards(1);
				TileEntity canidate = connected.getWorldObj().getTileEntity((int) p.x, (int) p.y, (int) p.z);
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
