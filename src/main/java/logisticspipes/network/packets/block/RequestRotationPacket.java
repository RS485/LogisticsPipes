package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class RequestRotationPacket extends CoordinatesPacket {

	public RequestRotationPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestRotationPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IRotationProvider tile = this.getTileOrPipe(player.world, IRotationProvider.class);
		if (tile != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(Rotation.class).setInteger(tile.getRotation()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), player);
		}
	}
}
