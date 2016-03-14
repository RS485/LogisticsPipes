package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.List;

import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;

import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

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
		for (EnumFacing direction : EnumFacing.VALUES) {
			DoubleCoordinates p = CoordinateUtils.add(new DoubleCoordinates(tile), direction);
			TileEntity canidate = p.getTileEntity(tile.getWorld());
			if (canidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(tile, canidate, direction)) {
				if (onlyOnePipe) {
					onlyOnePipe = false;
					break;
				} else {
					onlyOnePipe = true;
				}
			}
		}
		if (!onlyOnePipe || !SimpleServiceLocator.enderIOProxy.isSendAndReceive(tile)) {
			return new ArrayList<>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.enderIOProxy.getConnectedHyperCubes(tile);
		List<TileEntity> list = new ArrayList<>();
		for (TileEntity connected : connections) {
			if (!SimpleServiceLocator.enderIOProxy.isSendAndReceive(connected)) {
				continue;
			}
			LogisticsTileGenericPipe pipe = null;
			for (EnumFacing direction : EnumFacing.VALUES) {
				DoubleCoordinates p = CoordinateUtils.add(new DoubleCoordinates(connected), direction);
				TileEntity canidate = p.getTileEntity(tile.getWorld());
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
			return new ArrayList<>(0);
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
