package logisticspipes.network.packets.cpipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class CraftingPipeOpenConnectedGuiPacket extends CoordinatesPacket {

	public CraftingPipeOpenConnectedGuiPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeOpenConnectedGuiPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
				((PipeItemsCraftingLogistics) pipe.pipe).openAttachedGui(player);
			}
		}
	}
}

