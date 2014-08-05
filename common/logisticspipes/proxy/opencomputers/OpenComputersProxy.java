package logisticspipes.proxy.opencomputers;

import net.minecraft.nbt.NBTTagCompound;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Visibility;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.interfaces.IOpenComputersProxy;

public class OpenComputersProxy implements IOpenComputersProxy {
	
	@Override
	public void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile) {
		tile.node = Network.newNode(tile, Visibility.Neighbors).withComponent("logisticspipe", Visibility.Neighbors).create();
	}
	
	@Override
	public void addToNetwork(LogisticsTileGenericPipe tile) {
		Network.joinOrCreateNetwork(tile);
	}
	
	@Override
	public void handleLPInvalidate(LogisticsTileGenericPipe tile) {
		if(tile.node != null) tile.node.remove();
	}
	
	@Override
	public void handleLPChunkUnload(LogisticsTileGenericPipe tile) {
		if(tile.node != null) tile.node.remove();
	}
	
	@Override
	public void handleLPReadFromNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt) {
		if(tile.node != null && tile.node.host() == tile) {
			tile.node.load(nbt.getCompoundTag("oc:node"));
		}
	}
	
	@Override
	public void handleLPWriteToNBT(LogisticsTileGenericPipe tile, NBTTagCompound nbt) {
		if(tile.node != null && tile.node.host() == tile) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			tile.node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}
}
