package logisticspipes.network.packets.pipe;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemStack;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class PipeContentPacket extends ModernPacket {

	@Getter
	@Setter
	private ItemStack item;
	@Getter
	@Setter
	private int travelId;

	public PipeContentPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		item = input.readItemStack();
		travelId = input.readInt();
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
			LPTravelingItem.clientList.put(travelId, new WeakReference<>(content));
			synchronized (LPTravelingItem.forceKeep) {
				LPTravelingItem.forceKeep.add(new Tuple2<>(10, content)); //Keep in memory for min 10 ticks
			}
		} else {
			content.setItem(item);
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeItemStack(item);
		output.writeInt(travelId);
	}

	@Override
	public ModernPacket template() {
		return new PipeContentPacket(getId());
	}
}
