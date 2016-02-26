package logisticspipes.transport;

import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class PipeMultiBlockTransportLogistics extends PipeTransportLogistics {

	private CoreMultiBlockPipe multiPipe;

	public PipeMultiBlockTransportLogistics() {
		super(false);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe != null && ((LogisticsTileGenericPipe) tile).pipe.isHSTube()) {
			return true;
		}
		if (tile instanceof LogisticsTileGenericSubMultiBlock && ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe() != null && !((LogisticsTileGenericSubMultiBlock) tile).getMainPipe().isEmpty()) {
			for(LogisticsTileGenericPipe pipe: ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe()) {
				if(pipe.pipe == null || !pipe.pipe.isHSTube()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public CoreMultiBlockPipe getMultiPipe() {
		if (multiPipe == null) {
			CoreUnroutedPipe uPipe = getPipe();
			if (uPipe instanceof CoreMultiBlockPipe) {
				multiPipe = (CoreMultiBlockPipe) uPipe;
			}
		}
		return multiPipe;
	}

	@Override
	public float getPipeLength() {
		if (getMultiPipe() != null) {
			return getMultiPipe().getPipeLength();
		}
		return super.getPipeLength();
	}

	@Override
	public float getYawDiff(LPTravelingItem item) {
		if (getMultiPipe() != null) {
			return getMultiPipe().getYawDiff(item);
		}
		return super.getYawDiff(item);
	}

	@Override
	public ForgeDirection resolveDestination(LPTravelingItemServer data) {
		if (getMultiPipe() == null) {
			return null;
		}
		return getMultiPipe().getExitForInput(data.input.getOpposite());
	}

	@Override
	protected void reachedEnd(LPTravelingItem item) {
		TileEntity tile = null;
		if (getMultiPipe() != null) {
			tile = getMultiPipe().getConnectedEndTile(item.output);
		}
		if (items.scheduleRemoval(item)) {
			if (MainProxy.isServer(container.getWorldObj())) {
				handleTileReachedServer((LPTravelingItemServer) item, tile, item.output);
			} else {
				handleTileReachedClient((LPTravelingItemClient) item, tile, item.output);
			}
		}
	}

	@Override
	protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile, ForgeDirection dir) {
		markChunkModified(tile);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof CoreMultiBlockPipe) {
			passToNextPipe(arrivingItem, tile);
			return;
		} else if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> masterTile = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			if (!masterTile.isEmpty()) {
				if(masterTile.size() > 1) {
					throw new UnsupportedOperationException();
				}
				passToNextPipe(arrivingItem, masterTile.get(0));
				return;
			}
		}
		//TODO UNKNOWN TILE => destroy item in explosion
	}

	@Override
	protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile, ForgeDirection dir) {
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof CoreMultiBlockPipe) {
			passToNextPipe(arrivingItem, tile);
			return;
		} else if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> masterTile = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			if (!masterTile.isEmpty()) {
				if(masterTile.size() > 1) {
					throw new UnsupportedOperationException();
				}
				passToNextPipe(arrivingItem, masterTile.get(0));
				return;
			}
		}
	}

	@Override
	public void readjustSpeed(LPTravelingItemServer item) {
		item.setSpeed(0.8F);
	}

	@Override
	public CoreUnroutedPipe getNextPipe(ForgeDirection output) {
		TileEntity tile = null;
		if (getMultiPipe() != null) {
			tile = getMultiPipe().getConnectedEndTile(output);
		}
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> list = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			if(!list.isEmpty()) {
				if(list.size() > 1) {
					throw new UnsupportedOperationException();
				}
				tile = list.get(0);
			}
		}
		if (tile instanceof LogisticsTileGenericPipe) {
			return ((LogisticsTileGenericPipe) tile).pipe;
		}
		return null;
	}
}
