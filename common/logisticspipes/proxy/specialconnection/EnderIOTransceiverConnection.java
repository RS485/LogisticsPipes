package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

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
		for (EnumFacing direction : EnumFacing.VALUES) {
			DoubleCoordinates p = CoordinateUtils.add(new DoubleCoordinates(tile), direction);
			TileEntity candidate = p.getTileEntity(tile.getWorld());
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
			return new ArrayList<>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.enderIOProxy.getConnectedTransceivers(tile);
		Set<TileEntity> set = new HashSet<>();
		for (TileEntity connected : connections) {
			if (!SimpleServiceLocator.enderIOProxy.isSendAndReceive(connected)) {
				continue;
			}
			LogisticsTileGenericPipe pipe = null;
			for (EnumFacing direction : EnumFacing.VALUES) {
				DoubleCoordinates p = CoordinateUtils.add(new DoubleCoordinates(connected), direction);
				TileEntity candidate = p.getTileEntity(tile.getWorld());
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
			return new ArrayList<>(0);
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
			final CoreRoutedPipe pipeTile = (CoreRoutedPipe) ((LogisticsTileGenericPipe) pipe).pipe;
			final ItemIdentifierStack copiedStack = new ItemIdentifierStack(data.getItemIdentifierStack());
			pipeTile.queueUnroutedItemInformation(copiedStack, data.getInfo());
		} else {
			new RuntimeException("Only LP pipes can be next to transceiver to queue item information").printStackTrace();
		}
	}
}
