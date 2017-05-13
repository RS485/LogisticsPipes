package logisticspipes.network.packets.block;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class SolderingStationHeat extends IntegerCoordinatesPacket {

	public SolderingStationHeat(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SolderingStationHeat(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsSolderingTileEntity tile = this.getTile(player.worldObj, LogisticsSolderingTileEntity.class);
		if (tile != null) {
			int old = tile.heat;
			tile.heat = getInteger();
			if ((tile.heat == 0 && old != 0) || (tile.heat != 0 && old == 0)) {
				player.worldObj.notifyNeighborsRespectDebug(new BlockPos(getPosX(), getPosY(), getPosZ()), player.worldObj.getBlockState(new BlockPos(getPosX(), getPosY(), getPosZ())).getBlock());
			}
		}
	}
}
