package logisticspipes.network.packets.pipe;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.IntegerPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class PipeContentRequest extends IntegerPacket {

	public PipeContentRequest(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		WeakReference<LPTravelingItemServer> ref = LPTravelingItem.serverList.get(getInteger());
		LPTravelingItemServer item = null;
		if (ref != null) {
			item = ref.get();
		}
		if (item != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeContentPacket.class).setItem(item.getItemIdentifierStack()).setTravelId(item.getId()), player);
		}
	}

	@Override
	public ModernPacket template() {
		return new PipeContentRequest(getId());
	}
}
