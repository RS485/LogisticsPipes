package logisticspipes.network.packets.pipe;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ChassisOrientationPacket extends CoordinatesPacket {

	@Getter
	@Setter
	@Nullable
	private EnumFacing dir;

	public ChassisOrientationPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.world, LTGPCompletionCheck.PIPE);
		if (pipe.pipe instanceof PipeLogisticsChassis) {
			((PipeLogisticsChassis) pipe.pipe).setPointedOrientation(dir);
		}
	}

	@Override
	public ModernPacket template() {
		return new ChassisOrientationPacket(getId());
	}
}
