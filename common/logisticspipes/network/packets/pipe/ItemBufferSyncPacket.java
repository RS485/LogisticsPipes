package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ListSyncPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.entity.player.EntityPlayer;

public class ItemBufferSyncPacket extends ListSyncPacket<Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>> {

	public ItemBufferSyncPacket(int id, int x, int y, int z) {
		super(id, x, y, z);
	}

	public ItemBufferSyncPacket(int id) {
		super(id);
	}

	@Override
	public void writeObject(LPDataOutputStream data, Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer> object) throws IOException {
		data.writeItemIdentifierStack(object.getValue1());
	}

	@Override
	public Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer> readObject(LPDataInputStream data) throws IOException {
		return new Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>(data.readItemIdentifierStack(), null, null);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null || pipe.pipe == null || pipe.pipe.transport == null) {
			return;
		}
		pipe.pipe.transport._itemBuffer.clear();
		pipe.pipe.transport._itemBuffer.addAll(getList());
	}

	@Override
	public ItemBufferSyncPacket template() {
		return new ItemBufferSyncPacket(getId(), getPosX(), getPosY(), getPosZ());
	}
}
