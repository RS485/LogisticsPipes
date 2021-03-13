package logisticspipes.routing.pathfinder.changedetection;

import java.util.ArrayList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.LPTickHandler.LPWorldInfo;
import logisticspipes.ticks.QueuedTasks;
import network.rs485.logisticspipes.connection.ConnectionType;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class TEControl {

	public static void validate(final TileEntity tile) {
		final World world = tile.getWorld();
		if (!MainProxy.isServer(world)) {
			return;
		}
		if (tile.getClass().getName().startsWith("net.minecraft.tileentity")) {
			return;
		}

		final DoubleCoordinates pos = new DoubleCoordinates(tile);
		if (pos.getXInt() == 0 && pos.getYInt() <= 0 && pos.getZInt() == 0) {
			return;
		}

		if (SimpleServiceLocator.pipeInformationManager.isPipe(tile, false, ConnectionType.UNDEFINED) || SimpleServiceLocator.specialtileconnection.isType(tile)) {
			((ILPTEInformation) tile).setObject(new LPTileEntityObject());
			((ILPTEInformation) tile).getObject().initialised = LPTickHandler.getWorldInfo(world).getWorldTick();
			if (((ILPTEInformation) tile).getObject().initialised < 5) {
				return;
			}
			QueuedTasks.queueTask(() -> {
				if (!SimpleServiceLocator.pipeInformationManager.isPipe(tile, true, ConnectionType.UNDEFINED)) {
					return null;
				}
				for (EnumFacing dir : EnumFacing.VALUES) {
					DoubleCoordinates newPos = CoordinateUtils.sum(pos, dir);
					if (!newPos.blockExists(world)) {
						continue;
					}
					TileEntity nextTile = newPos.getTileEntity(world);
					if (nextTile != null && ((ILPTEInformation) nextTile).getObject() != null) {
						if (SimpleServiceLocator.pipeInformationManager.isItemPipe(nextTile)) {
							SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(nextTile).refreshTileCacheOnSide(dir.getOpposite());
						}
						if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
							SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(tile).refreshTileCacheOnSide(dir);
							SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(tile).refreshTileCacheOnSide(dir.getOpposite());
						}
						for (ITileEntityChangeListener listener : new ArrayList<>(((ILPTEInformation) nextTile).getObject().changeListeners)) {
							listener.pipeAdded(pos, dir.getOpposite());
						}
					}
				}
				return null;
			});
		}
	}

	public static void invalidate(final TileEntity tile) {
		final World world = tile.getWorld();
		if (!MainProxy.isServer(world)) {
			return;
		}
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isRoutingPipe()) {
			return;
		}
		if (((ILPTEInformation) tile).getObject() != null) {
			QueuedTasks.queueTask(() -> {
				DoubleCoordinates pos = new DoubleCoordinates(tile);
				for (EnumFacing dir : EnumFacing.VALUES) {
					DoubleCoordinates newPos = CoordinateUtils.sum(pos, dir);
					if (!newPos.blockExists(world)) {
						continue;
					}
					TileEntity nextTile = newPos.getTileEntity(world);
					if (nextTile != null && ((ILPTEInformation) nextTile).getObject() != null) {
						if (SimpleServiceLocator.pipeInformationManager.isItemPipe(nextTile)) {
							SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(nextTile).refreshTileCacheOnSide(dir.getOpposite());
						}
					}
				}
				for (ITileEntityChangeListener listener : new ArrayList<>(((ILPTEInformation) tile).getObject().changeListeners)) {
					listener.pipeRemoved(pos);
				}
				return null;
			});
		}
	}

	public static void handleBlockUpdate(final World world, final LPWorldInfo info, BlockPos blockPos) {
		if (info.isSkipBlockUpdateForWorld()) {
			return;
		}
		if (info.getWorldTick() < 5) {
			return;
		}
		final DoubleCoordinates pos = new DoubleCoordinates(blockPos);
		if (info.getUpdateQueued().contains(pos)) {
			return;
		}
		if (!pos.blockExists(world)) {
			return;
		}
		final TileEntity tile = pos.getTileEntity(world);
		if (SimpleServiceLocator.enderIOProxy.isBundledPipe(tile)) {
			QueuedTasks.queueTask(() -> {
				for (EnumFacing dir : EnumFacing.VALUES) {
					DoubleCoordinates newPos = CoordinateUtils.sum(pos, dir);
					if (!newPos.blockExists(world)) {
						continue;
					}
					TileEntity nextTile = newPos.getTileEntity(world);
					if (nextTile instanceof LogisticsTileGenericPipe) {
						((LogisticsTileGenericPipe) nextTile).scheduleNeighborChange();
					}
				}
				return null;
			});
		}
		if (tile == null || ((ILPTEInformation) tile).getObject() == null) {
			return;
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile) || SimpleServiceLocator.specialtileconnection.isType(tile)) {
			info.getUpdateQueued().add(pos);
			QueuedTasks.queueTask(() -> {
				for (EnumFacing dir : EnumFacing.VALUES) {
					DoubleCoordinates newPos = CoordinateUtils.sum(pos, dir);
					if (!newPos.blockExists(world)) {
						continue;
					}
					TileEntity nextTile = newPos.getTileEntity(world);
					if (nextTile != null && ((ILPTEInformation) nextTile).getObject() != null) {
						for (ITileEntityChangeListener listener : new ArrayList<>(((ILPTEInformation) nextTile).getObject().changeListeners)) {
							listener.pipeModified(pos);
						}
					}
				}
				for (ITileEntityChangeListener listener : new ArrayList<>(((ILPTEInformation) tile).getObject().changeListeners)) {
					listener.pipeModified(pos);
				}
				info.getUpdateQueued().remove(pos);
				return null;
			});
		}
	}
}
