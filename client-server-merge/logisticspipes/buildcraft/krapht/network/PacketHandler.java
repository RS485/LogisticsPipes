package logisticspipes.buildcraft.krapht.network;

import logisticspipes.buildcraft.krapht.proxy.MainProxy;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		if(MainProxy.isClient()) {
			ClientPacketHandler.onPacketData(manager, packet, player);
		} else {
			ServerPacketHandler.onPacketData(manager, packet, player);
		}
	}
}
