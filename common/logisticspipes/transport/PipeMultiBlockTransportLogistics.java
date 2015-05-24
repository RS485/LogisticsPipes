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

public class PipeMultiBlockTransportLogistics extends PipeTransportLogistics {

	private CoreMultiBlockPipe multiPipe;

	public PipeMultiBlockTransportLogistics() {
		super(false);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe != null && ((LogisticsTileGenericPipe)tile).pipe.isMultiBlock()) {
			return true;
		}
		if(tile instanceof LogisticsTileGenericSubMultiBlock &&((LogisticsTileGenericSubMultiBlock)tile).getMainPipe() != null && ((LogisticsTileGenericSubMultiBlock)tile).getMainPipe().pipe != null && ((LogisticsTileGenericSubMultiBlock)tile).getMainPipe().pipe.isMultiBlock()) {
			return true;
		}
		return false;
	}

	public CoreMultiBlockPipe getMultiPipe() {
		if(multiPipe == null) {
			CoreUnroutedPipe uPipe = getPipe();
			if(uPipe instanceof CoreMultiBlockPipe) {
				multiPipe = (CoreMultiBlockPipe) uPipe;
			}
		}
		return multiPipe;
	}

	@Override
	public float getPipeLength() {
		if(getMultiPipe() != null) {
			return getMultiPipe().getPipeLength();
		}
		return super.getPipeLength();
	}

	@Override
	public ForgeDirection resolveDestination(LPTravelingItemServer data) {
		if(getMultiPipe() == null) return null;
		return getMultiPipe().getExitForInput(data.input.getOpposite());
	}
	
	@Override
	protected void reachedEnd(LPTravelingItem item) {
		TileEntity tile = null;
		if(getMultiPipe() != null) {
			tile = getMultiPipe().getConnectedEndTile(item.output);
		}
		if(items.scheduleRemoval(item)) {
			if(MainProxy.isServer(container.getWorldObj())) {
				handleTileReachedServer((LPTravelingItemServer)item, tile, item.output);
			} else {
				handleTileReachedClient((LPTravelingItemClient)item, tile);
			}
		}
	}

	@Override
	protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile, ForgeDirection dir) {
		this.markChunkModified(tile);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof CoreMultiBlockPipe) {
			passToNextPipe(arrivingItem, tile);
			return;
		} else if(tile instanceof LogisticsTileGenericSubMultiBlock) {
			LogisticsTileGenericPipe masterTile = ((LogisticsTileGenericSubMultiBlock)tile).getMainPipe();
			if(masterTile != null) {
				passToNextPipe(arrivingItem, tile);
				return;
			}
		}
		//TODO UNKNOWN TILE => destroy item in explosion
	}

	@Override
	protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile) {
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof CoreMultiBlockPipe) {
			passToNextPipe(arrivingItem, tile);
			return;
		} else if(tile instanceof LogisticsTileGenericSubMultiBlock) {
			LogisticsTileGenericPipe masterTile = ((LogisticsTileGenericSubMultiBlock)tile).getMainPipe();
			if(masterTile != null) {
				passToNextPipe(arrivingItem, tile);
				return;
			}
		}
	}
}
