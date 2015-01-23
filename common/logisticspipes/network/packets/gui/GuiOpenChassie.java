package logisticspipes.network.packets.gui;

import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.pipe.ChassiGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class GuiOpenChassie extends CoordinatesPacket {
	public GuiOpenChassie(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if(!(pipe.pipe instanceof CoreRoutedPipe)) return;
		NewGuiHandler.getGui(ChassiGuiProvider.class).setFlag(((CoreRoutedPipe)pipe.pipe).getUpgradeManager().hasUpgradeModuleUpgrade()).setSlot(ModulePositionType.IN_PIPE).setPositionInt(0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
	}

	@Override
	public ModernPacket template() {
		return new GuiOpenChassie(getId());
	}
}
