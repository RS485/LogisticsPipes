package logisticspipes.routing.pathfinder.changedetection;

import java.util.ArrayList;

import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.LPTickHandler.LPWorldInfo;
import logisticspipes.ticks.QueuedTasks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class TEControl {

	public static void validate(final TileEntity tile) {
		final World world = tile.getWorldObj();
		if (world == null) {
			return;
		}
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

		if (SimpleServiceLocator.pipeInformationManager.isPipe(tile, false, ConnectionPipeType.UNDEFINED) || SimpleServiceLocator.specialtileconnection.isType(tile)) {
			((ILPTEInformation) tile).setObject(new LPTileEntityObject());
			((ILPTEInformation) tile).getObject().initialised = LPTickHandler.getWorldInfo(world).getWorldTick();
			if (((ILPTEInformation) tile).getObject().initialised < 5) {
				return;
			}
			QueuedTasks.queueTask(() -> {
				if (!SimpleServiceLocator.pipeInformationManager.isPipe(tile, true, ConnectionPipeType.UNDEFINED)) {
					return null;
				}
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
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
		final World world = tile.getWorldObj();
		if (world == null) {
			return;
		}
		if (!MainProxy.isServer(world)) {
			return;
		}
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isRoutingPipe()) {
			return;
		}
		if (((ILPTEInformation) tile).getObject() != null) {
			QueuedTasks.queueTask(() -> {
				DoubleCoordinates pos = new DoubleCoordinates(tile);
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
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

	private static boolean block = false;

	public static void notifyBlocksOfNeighborChange_Start(World world, int x, int y, int z) {
		TEControl.block = true;
		if (!MainProxy.isServer(world)) {
			return;
		}
		TEControl.handleBlockUpdate(world, LPTickHandler.getWorldInfo(world), x, y, z);
	}

	public static void notifyBlocksOfNeighborChange_Stop(World world, int x, int y, int z) {
		TEControl.block = false;
	}

	public static void notifyBlockOfNeighborChange(World world, int x, int y, int z) {
		if (TEControl.block) {
			return;
		}
		TEControl.handleBlockUpdate(world, LPTickHandler.getWorldInfo(world), x, y, z);
	}

	public static void handleBlockUpdate(final World world, final LPWorldInfo info, int x, int y, int z) {
		if (info.getWorldTick() < 5) {
			return;
		}
		final DoubleCoordinates pos = new DoubleCoordinates(x, y, z);
		if (info.getUpdateQueued().contains(pos)) {
			return;
		}
		if (!pos.blockExists(world)) {
			return;
		}
		final TileEntity tile = pos.getTileEntity(world);
		if (tile == null || ((ILPTEInformation) tile).getObject() == null) {
			return;
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile) || SimpleServiceLocator.specialtileconnection.isType(tile)) {
			info.getUpdateQueued().add(pos);
			QueuedTasks.queueTask(() -> {
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
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
