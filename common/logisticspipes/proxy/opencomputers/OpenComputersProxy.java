package logisticspipes.proxy.opencomputers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.interfaces.IOpenComputersProxy;

public class OpenComputersProxy implements IOpenComputersProxy {

	@Override
	public void initLogisticsTileGenericPipe(LogisticsTileGenericPipe tile) {
		tile.node = Network.newNode(tile, Visibility.Neighbors).withComponent("logisticspipe", Visibility.Neighbors).create();
	}

	@Override
	public void initLogisticsSolidTileEntity(LogisticsSolidTileEntity tile) {
		tile.node = Network.newNode(tile, Visibility.Neighbors).withComponent("logisticssolidblock", Visibility.Neighbors).create();
	}

	@Override
	public void addToNetwork(TileEntity tile) {
		Network.joinOrCreateNetwork(tile);
	}

	@Override
	public void handleInvalidate(IOCTile tile) {
		if (tile.getOCNode() != null) {
			((Node) tile.getOCNode()).remove();
		}
	}

	@Override
	public void handleChunkUnload(IOCTile tile) {
		if (tile.getOCNode() != null) {
			((Node) tile.getOCNode()).remove();
		}
	}

	@Override
	public void handleReadFromNBT(IOCTile tile, NBTTagCompound nbt) {
		if (tile.getOCNode() != null && ((Node) tile.getOCNode()).host() == tile) {
			((Node) tile.getOCNode()).load(nbt.getCompoundTag("oc:node"));
		}
	}

	@Override
	public void handleWriteToNBT(IOCTile tile, NBTTagCompound nbt) {
		if (tile.getOCNode() != null && ((Node) tile.getOCNode()).host() == tile) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			((Node) tile.getOCNode()).save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}
}
