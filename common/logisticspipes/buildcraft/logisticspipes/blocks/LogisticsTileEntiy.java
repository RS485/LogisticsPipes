package logisticspipes.buildcraft.logisticspipes.blocks;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.buildcraft.krapht.CoreRoutedPipe;
import logisticspipes.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import logisticspipes.buildcraft.krapht.proxy.MainProxy;


import net.minecraft.src.TileEntity;
import buildcraft.transport.TileGenericPipe;

public class LogisticsTileEntiy extends TileEntity {
	
	private boolean init = false;
	
	public CoreRoutedPipe[] getNearRoutingPipes() {
		List<CoreRoutedPipe> list = new ArrayList<CoreRoutedPipe>();
		TileEntity tile = worldObj.getBlockTileEntity(xCoord + 1,yCoord,zCoord);
		if(tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
			list.add((CoreRoutedPipe) ((TileGenericPipe)tile).pipe);
		}
		tile = worldObj.getBlockTileEntity(xCoord - 1,yCoord,zCoord);
		if(tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
			list.add((CoreRoutedPipe) ((TileGenericPipe)tile).pipe);
		}
		tile = worldObj.getBlockTileEntity(xCoord,yCoord,zCoord + 1);
		if(tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
			list.add((CoreRoutedPipe) ((TileGenericPipe)tile).pipe);
		}
		tile = worldObj.getBlockTileEntity(xCoord,yCoord,zCoord - 1);
		if(tile instanceof TileGenericPipe && ((TileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
			list.add((CoreRoutedPipe) ((TileGenericPipe)tile).pipe);
		}
		return list.toArray(new CoreRoutedPipe[]{});
	}
	
    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
    public void updateEntity() {
    	if(!init) {
    		init = true;
    		if(MainProxy.isClient()) {
	    		for(CoreRoutedPipe pipe:getNearRoutingPipes()) {
	    			if(pipe instanceof PipeItemsCraftingLogistics) {
	    				((PipeItemsCraftingLogistics)pipe).enableUpdateRequest();
	    			}
	    		}
    		}
    	}
    }

	public PipeItemsCraftingLogistics getAttachedSignOwnerPipe() {
		for(CoreRoutedPipe pipe:this.getNearRoutingPipes()) {
			if(pipe instanceof PipeItemsCraftingLogistics) {
				if(((PipeItemsCraftingLogistics)pipe).isAttachedSign(this)) {
					return (PipeItemsCraftingLogistics)pipe;
				}
			}
		}
		return null;
	}
}
