package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ChassiOrientationPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ForgeDirection dir;

	public ChassiOrientationPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		((PipeLogisticsChassi) pipe.pipe).setClientOrientation(dir);
	}

	@Override
	public ModernPacket template() {
		return new ChassiOrientationPacket(getId());
	}
}
