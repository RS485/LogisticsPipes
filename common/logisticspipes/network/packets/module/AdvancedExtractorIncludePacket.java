package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.packets.modules.AdvancedExtractorInclude;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

public class AdvancedExtractorIncludePacket extends ModuleCoordinatesPacket {

	public AdvancedExtractorIncludePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorIncludePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleAdvancedExtractor module = this.getLogisticsModule(player, ModuleAdvancedExtractor.class);
		if (module == null) {
			return;
		}
		module.setItemsIncluded(!module.areItemsIncluded());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setFlag(module.areItemsIncluded()).setPacketPos(this), player);
	}
}
