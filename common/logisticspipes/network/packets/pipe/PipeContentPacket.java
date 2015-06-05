package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.lang.ref.WeakReference;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PipeContentPacket extends ModernPacket {

	public PipeContentPacket(int id) {
		super(id);
	}

	@Getter
	@Setter
	private ItemIdentifierStack item;
	@Getter
	@Setter
	private int travelId;

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		item = data.readItemIdentifierStack();
		travelId = data.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		WeakReference<LPTravelingItemClient> ref = LPTravelingItem.clientList.get(travelId);
		LPTravelingItemClient content = null;
		if (ref != null) {
			content = ref.get();
		}
		if (content == null) {
			content = new LPTravelingItemClient(travelId, item);
			LPTravelingItem.clientList.put(travelId, new WeakReference<LPTravelingItemClient>(content));
			synchronized (LPTravelingItem.forceKeep) {
				LPTravelingItem.forceKeep.add(new Pair<Integer, Object>(10, content)); //Keep in memory for min 10 ticks
			}
		} else {
			content.setItem(item);
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeItemIdentifierStack(item);
		data.writeInt(travelId);
	}

	@Override
	public ModernPacket template() {
		return new PipeContentPacket(getId());
	}
}
