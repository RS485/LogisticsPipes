package logisticspipes.network;

import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		if(packet.data == null) {
			new Exception("Packet content has been null").printStackTrace();
		}
		if(MainProxy.isClient(((EntityPlayer)player).worldObj)) {
			ClientPacketHandler.onPacketData(manager, packet, player);
		} else {
			ServerPacketHandler.onPacketData(manager, packet, player);
		}
	}
}
