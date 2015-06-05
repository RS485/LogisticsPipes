package logisticspipes.network.packets.block;

import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

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
		IRotationProvider tile = this.getTileOrPipe(player.worldObj, IRotationProvider.class);
		if (tile != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(Rotation.class).setInteger(tile.getRotation()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), player);
		}
	}
}
