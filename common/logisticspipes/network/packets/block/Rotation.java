package logisticspipes.network.packets.block;

import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.client.FMLClientHandler;

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
		IRotationProvider tile = this.getTileOrPipe(player.worldObj, IRotationProvider.class);
		if (tile != null) {
			tile.setRotation(getInteger());
			player.worldObj.notifyNeighborsRespectDebug(new BlockPos(getPosX(), getPosY(), getPosZ()), player.worldObj.getBlockState(new BlockPos(getPosX(), getPosY(), getPosZ())).getBlock());
		}
	}
}
