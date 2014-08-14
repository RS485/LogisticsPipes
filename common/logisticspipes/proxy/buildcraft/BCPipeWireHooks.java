package logisticspipes.proxy.buildcraft;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportStructure;

public class BCPipeWireHooks {
	public interface PipeClassReceiveSignal {
		public boolean receiveSignal(int signal, PipeWire color);
		public void triggerInternalUpdateScheduled();
	}

	public static boolean isWireConnectedTo(Pipe<?> pipe, TileEntity tile, PipeWire color) {
		if(!(tile instanceof LogisticsTileGenericPipe)) return false;
		LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
		if (!LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
			return false;
		}

		if (!tilePipe.pipe.bcPipePart.getWireSet()[color.ordinal()]) {
			return false;
		}

		return pipe.transport instanceof PipeTransportStructure || Utils.checkPipesConnections(pipe.container, tile);
	}
	
	public static void updateSignalStateForColor(Pipe<?> pipe, PipeWire color) {
		if (pipe.signalStrength[color.ordinal()] > 1) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);

				if (tile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
					
					if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe) && tilePipe.pipe.bcPipePart.getWireSet()[color.ordinal()]) {
						if (pipe.isWireConnectedTo(tile, color)) {
							tilePipe.pipe.bcPipePart.receiveSignal(pipe.signalStrength[color.ordinal()] - 1, color);
						}
					}
				}
			}
		}
	}

	public static boolean readNearbyPipesSignal_Pre(Pipe<?> pipe, PipeWire color) {
		pipeSignalStrengthCache = pipe.signalStrength.clone();
		boolean foundBiggerSignal = false;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = pipe.container.getTile(o);

			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;

				if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
					if (pipe.isWireConnectedTo(tile, color)) {
						foundBiggerSignal |= ((PipeClassReceiveSignal)pipe).receiveSignal(tilePipe.pipe.bcPipePart.getSignalStrength()[color.ordinal()] - 1, color);
					}
				}
			}
		}
		return foundBiggerSignal;
	}

	public static int[] pipeSignalStrengthCache = null;

	public static void readNearbyPipesSignal_Post(Pipe<?> pipe, PipeWire color, boolean foundBiggerSignal) {
		if (!foundBiggerSignal && pipeSignalStrengthCache[color.ordinal()] != 0) {
			pipe.container.scheduleRenderUpdate();

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);

				
				if (tile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
					
					if (LogisticsBlockGenericPipe.isFullyDefined(tilePipe.pipe)) {
						tilePipe.pipe.internalUpdateScheduled = true;
					}
				}
			}
		}
	}
}
