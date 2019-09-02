package logisticspipes.network.packets;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.DimensionManager;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class PlayerListRequest extends ModernPacket {

	public PlayerListRequest(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PlayerListRequest(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		Stream<?> allPlayers = Arrays.stream(DimensionManager.getWorlds()).map(worldServer -> worldServer.playerEntities).flatMap(Collection::stream);
		Stream<EntityPlayer> allPlayerEntities = allPlayers.filter(o -> o instanceof EntityPlayer).map(o -> (EntityPlayer) o);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PlayerList.class)
				.setStringList(allPlayerEntities.map(entityPlayer -> entityPlayer.getGameProfile().getName()).collect(Collectors.toList())), player);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
	public void writeData(LPDataOutput output) {}
}
