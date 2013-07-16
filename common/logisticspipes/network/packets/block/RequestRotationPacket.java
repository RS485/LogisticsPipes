package logisticspipes.network.packets.block;

import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

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
		IRotationProvider tile = this.getTile(player.worldObj, IRotationProvider.class);
		if(tile != null) {
//TODO		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.ROTATION_SET, getPosX(), getPosY(), getPosZ(), tile.getRotation()).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(Rotation.class).setInteger(tile.getRotation()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player)player);
		}
	}
}

