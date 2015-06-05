package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class EnderIOTransceiverConnection implements ISpecialTileConnection {

	@Override
	public boolean init() {
		return SimpleServiceLocator.enderIOProxy.isEnderIO();
	}

	@Override
	public boolean isType(TileEntity tile) {
		return SimpleServiceLocator.enderIOProxy.isTransceiver(tile);
	}

	@Override
	public Collection<TileEntity> getConnections(TileEntity tile) {
		boolean onlyOnePipe = false;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			LPPosition p = new LPPosition(tile);
			p.moveForward(direction);
			TileEntity candidate = p.getTileEntity(tile.getWorldObj());
			if (candidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(tile, candidate, direction)) {
				if (onlyOnePipe) {
					onlyOnePipe = false;
					break;
				} else {
					onlyOnePipe = true;
				}
			}
		}
		if (!onlyOnePipe || !SimpleServiceLocator.enderIOProxy.isSendAndReceive(tile)) {
			return new ArrayList<TileEntity>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.enderIOProxy.getConnectedTransceivers(tile);
		Set<TileEntity> set = new HashSet<TileEntity>();
		for (TileEntity connected : connections) {
			if (!SimpleServiceLocator.enderIOProxy.isSendAndReceive(connected)) {
				continue;
			}
			LogisticsTileGenericPipe pipe = null;
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				LPPosition p = new LPPosition(connected);
				p.moveForward(direction);
				TileEntity candidate = p.getTileEntity(tile.getWorldObj());
				if (candidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(connected, candidate, direction)) {
					if (pipe != null) {
						pipe = null;
						break;
					} else {
						pipe = (LogisticsTileGenericPipe) candidate;
					}
				}
			}
			if (pipe != null && pipe.pipe instanceof CoreRoutedPipe) {
				set.add(pipe);
			}
		}
		if (set.size() == 1) {
			return set;
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
		Collection<TileEntity> list = getConnections(tile);
		if (list.size() < 1) {
			return;
		}
		TileEntity pipe = list.iterator().next();
		if (pipe instanceof LogisticsTileGenericPipe) {
			((CoreRoutedPipe) ((LogisticsTileGenericPipe) pipe).pipe).queueUnroutedItemInformation(data.getItemIdentifierStack().clone(), data.getInfo());
		} else {
			new RuntimeException("Only LP pipes can be next to transceiver to queue item information").printStackTrace();
		}
	}
}
