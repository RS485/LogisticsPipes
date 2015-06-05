package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.ProviderPipeMode;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

public class ProviderPipeNextModePacket extends CoordinatesPacket {

	public ProviderPipeNextModePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderPipeNextModePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		providerpipe.nextExtractionMode();
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProviderPipeMode.class).setInteger(providerpipe.getExtractionMode().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), player);
	}
}
