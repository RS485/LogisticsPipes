package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.tileentity.TileEntity;
import buildcraft.transport.TileGenericPipe;

public class LogisticsSignTileEntity extends TileEntity {
	
	private boolean init = false;
	
	public LogisticsSignTileEntity() {}
	
	@Override
	public void updateEntity() {
		if(!init) {
    		init = true;
    		if(MainProxy.isClient(worldObj)) {
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
	
	private CoreRoutedPipe[] getNearRoutingPipes() {
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
}
