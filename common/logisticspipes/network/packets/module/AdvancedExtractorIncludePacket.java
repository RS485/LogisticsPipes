package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.packets.modules.AdvancedExtractorInclude;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;

@StaticResolve
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
		final AsyncAdvancedExtractor module = this.getLogisticsModule(player, AsyncAdvancedExtractor.class);
		if (module == null) {
			return;
		}
		module.setItemsIncluded(!module.getItemsIncluded());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setFlag(module.getItemsIncluded()).setPacketPos(this), player);
	}
}
