package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.ListSyncPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ItemBufferSyncPacket
		extends ListSyncPacket<Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer>> {

	public ItemBufferSyncPacket(int id, int x, int y, int z) {
		super(id, x, y, z);
	}

	public ItemBufferSyncPacket(int id) {
		super(id);
	}

	@Override
	public void writeObject(LPDataOutput output,
			Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer> object) {
		output.writeItemIdentifierStack(object.getValue1());
	}

	@Override
	public Triplet<ItemIdentifierStack, Pair<Integer /* Time */, Integer /* BufferCounter */>, LPTravelingItemServer> readObject(LPDataInput input) {
		return new Triplet<>(input.readItemIdentifierStack(), null, null);
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
