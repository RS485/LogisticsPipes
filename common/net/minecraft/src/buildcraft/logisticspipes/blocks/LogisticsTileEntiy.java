package net.minecraft.src.buildcraft.logisticspipes.blocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public class LogisticsTileEntiy extends TileEntity {
	
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
