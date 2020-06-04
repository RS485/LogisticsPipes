package logisticspipes.network.packets.modules;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.BooleanModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;

@StaticResolve
public class AdvancedExtractorInclude extends BooleanModuleCoordinatesPacket {

	public AdvancedExtractorInclude(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorInclude(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		AsyncAdvancedExtractor recieiver = this.getLogisticsModule(player, AsyncAdvancedExtractor.class);
		if (recieiver == null) {
			return;
		}
		recieiver.setItemsIncluded(isFlag());
	}
}
