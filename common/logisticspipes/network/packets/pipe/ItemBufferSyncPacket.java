package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ListSyncPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.entity.player.EntityPlayer;

public class ItemBufferSyncPacket extends ListSyncPacket<Pair<ItemIdentifierStack, Pair<Integer, Integer>>> {

	public ItemBufferSyncPacket(int id, int x, int y, int z) {
		super(id, x, y, z);
	}
	
	public ItemBufferSyncPacket(int id) {
		super(id);
	}

	@Override
	public void writeObject(LPDataOutputStream data, Pair<ItemIdentifierStack, Pair<Integer, Integer>> object) throws IOException {
		data.writeItemIdentifierStack(object.getValue1());
	}

	@Override
	public Pair<ItemIdentifierStack, Pair<Integer, Integer>> readObject(LPDataInputStream data) throws IOException {
		return new Pair<ItemIdentifierStack, Pair<Integer, Integer>>(data.readItemIdentifierStack(), null);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if(pipe == null || pipe.pipe == null || pipe.pipe.transport == null) return;
		pipe.pipe.transport._itemBuffer.clear();
		pipe.pipe.transport._itemBuffer.addAll(getList());
	}

	@Override
	public ItemBufferSyncPacket template() {
		return new ItemBufferSyncPacket(getId(), getPosX(), getPosY(), getPosZ());
	}
}
