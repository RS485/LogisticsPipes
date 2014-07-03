package logisticspipes.network.packets.gui;

import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.guis.pipe.ChassiGuiProvider;
import net.minecraft.entity.player.EntityPlayer;

public class GuiOpenChassie extends ModuleCoordinatesPacket {
	public GuiOpenChassie(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		NewGuiHandler.getGui(ChassiGuiProvider.class).setSlot(ModulePositionType.IN_PIPE).setPositionInt(0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
	}

	@Override
	public ModernPacket template() {
		return new GuiOpenChassie(getId());
	}
}
