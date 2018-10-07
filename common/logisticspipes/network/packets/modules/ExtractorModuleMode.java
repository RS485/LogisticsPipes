package logisticspipes.network.packets.modules;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.abstractpackets.DirectionModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ExtractorModuleMode extends DirectionModuleCoordinatesPacket {

	public ExtractorModuleMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ExtractorModuleMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsSneakyDirectionModule recieiver = this.getLogisticsModule(player, LogisticsSneakyDirectionModule.class);
		if (recieiver == null) {
			return;
		}
		recieiver.setSneakyDirection(getDirection());
	}
}
