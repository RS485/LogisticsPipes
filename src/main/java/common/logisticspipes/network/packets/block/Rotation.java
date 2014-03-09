package logisticspipes.network.packets.block;

import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

public class Rotation extends IntegerCoordinatesPacket {

	public Rotation(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new Rotation(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IRotationProvider tile = this.getTile(player.worldObj, IRotationProvider.class);
		if(tile instanceof IRotationProvider) {
			tile.setRotation(getInteger());
			FMLClientHandler.instance().getClient().theWorld.markBlockForUpdate(getPosX(), getPosY(), getPosZ());
		}
	}
}

