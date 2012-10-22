package logisticspipes.network;

import logisticspipes.proxy.MainProxy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		if(MainProxy.isClient(((EntityPlayer)player).worldObj)) {
			ClientPacketHandler.onPacketData(manager, packet, player);
		} else {
			ServerPacketHandler.onPacketData(manager, packet, player);
		}
	}
}
