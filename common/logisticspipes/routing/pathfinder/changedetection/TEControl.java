package logisticspipes.routing.pathfinder.changedetection;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.LPTickHandler.LPWorldInfo;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.tuples.LPPosition;

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

		final LPPosition pos = new LPPosition(tile);
		if (pos.getX() == 0 && pos.getY() <= 0 && pos.getZ() == 0) {
			return;
		}

		if (SimpleServiceLocator.pipeInformationManager.isPipe(tile, false) || SimpleServiceLocator.specialtileconnection.isType(tile)) {
			((ILPTEInformation) tile).setObject(new LPTileEntityObject());
			((ILPTEInformation) tile).getObject().initialised = LPTickHandler.getWorldInfo(world).getWorldTick();
			if (((ILPTEInformation) tile).getObject().initialised < 5) {
				return;
			}
			QueuedTasks.queueTask(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					if (!SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
						return null;
					}
					for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
						LPPosition newPos = pos.copy();
						newPos.moveForward(dir);
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
							for (ITileEntityChangeListener listener : new ArrayList<ITileEntityChangeListener>(((ILPTEInformation) nextTile).getObject().changeListeners)) {
								listener.pipeAdded(pos, dir.getOpposite());
							}
						}
					}
					return null;
				}
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
			QueuedTasks.queueTask(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					LPPosition pos = new LPPosition(tile);
					for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
						LPPosition newPos = pos.copy();
						newPos.moveForward(dir);
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
					for (ITileEntityChangeListener listener : new ArrayList<ITileEntityChangeListener>(((ILPTEInformation) tile).getObject().changeListeners)) {
						listener.pipeRemoved(pos);
					}
					return null;
				}
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
		final LPPosition pos = new LPPosition(x, y, z);
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
			QueuedTasks.queueTask(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
						LPPosition newPos = pos.copy();
						newPos.moveForward(dir);
						if (!newPos.blockExists(world)) {
							continue;
						}
						TileEntity nextTile = newPos.getTileEntity(world);
						if (nextTile != null && ((ILPTEInformation) nextTile).getObject() != null) {
							for (ITileEntityChangeListener listener : new ArrayList<ITileEntityChangeListener>(((ILPTEInformation) nextTile).getObject().changeListeners)) {
								listener.pipeModified(pos);
							}
						}
					}
					for (ITileEntityChangeListener listener : new ArrayList<ITileEntityChangeListener>(((ILPTEInformation) tile).getObject().changeListeners)) {
						listener.pipeModified(pos);
					}
					info.getUpdateQueued().remove(pos);
					return null;
				}
			});
		}
	}
}
