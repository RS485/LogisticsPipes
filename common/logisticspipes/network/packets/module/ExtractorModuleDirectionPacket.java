package logisticspipes.network.packets.module;

import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.DirectionModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ExtractorModuleDirectionPacket extends DirectionModuleCoordinatesPacket {

	public ExtractorModuleDirectionPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ExtractorModuleDirectionPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsSneakyDirectionModule module = this.getLogisticsModule(player, LogisticsSneakyDirectionModule.class);
		if (module == null) {
			return;
		}
		module.setSneakyDirection(getDirection());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setDirection(module.getSneakyDirection()).setPacketPos(this), player);
	}
}
