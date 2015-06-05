package logisticspipes.network.packets.modules;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.abstractpackets.BooleanModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

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
		ModuleAdvancedExtractor recieiver = this.getLogisticsModule(player, ModuleAdvancedExtractor.class);
		if (recieiver == null) {
			return;
		}
		recieiver.setItemsIncluded(isFlag());
	}
}
