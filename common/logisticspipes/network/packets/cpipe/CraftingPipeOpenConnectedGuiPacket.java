package logisticspipes.network.packets.cpipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

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
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
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

