package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

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
			LPPosition p = new LPPosition(tile);
			p.moveForward(direction);
			TileEntity canidate = p.getTileEntity(tile.getWorldObj());
			if (canidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(tile, canidate, direction)) {
				if (onlyOnePipe) {
					onlyOnePipe = false;
					break;
				} else {
					onlyOnePipe = true;
				}
			}
		}
		if (!onlyOnePipe) {
			return new ArrayList<TileEntity>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.thermalExpansionProxy.getConnectedTesseracts(tile);
		connections.remove(tile);
		List<TileEntity> list = new ArrayList<TileEntity>();
		for (TileEntity connected : connections) {
			LogisticsTileGenericPipe pipe = null;
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				LPPosition p = new LPPosition(connected);
				p.moveForward(direction);
				TileEntity canidate = p.getTileEntity(connected.getWorldObj());
				if (canidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(connected, canidate, direction)) {
					if (pipe != null) {
						pipe = null;
						break;
					} else {
						pipe = (LogisticsTileGenericPipe) canidate;
					}
				}
			}
			if (pipe != null && pipe.pipe instanceof CoreRoutedPipe) {
				list.add(pipe);
			}
		}
		if (list.size() == 1) {
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
		if (list.size() < 1) {
			return;
		}
		TileEntity pipe = list.get(0);
		if (pipe instanceof LogisticsTileGenericPipe) {
			((CoreRoutedPipe) ((LogisticsTileGenericPipe) pipe).pipe).queueUnroutedItemInformation(data.getItemIdentifierStack().clone(), data.getInfo());
		} else {
			new RuntimeException("Only LP pipes can be next to Tesseracts to queue item information").printStackTrace();
		}
	}
}
