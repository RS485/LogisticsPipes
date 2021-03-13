package logisticspipes.network.packets.gui;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.pipe.ChassisGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class GuiOpenChassis extends CoordinatesPacket {

	public GuiOpenChassis(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe.pipe instanceof CoreRoutedPipe) {
			NewGuiHandler.getGui(ChassisGuiProvider.class).setFlag(pipe.pipe.getUpgradeManager().hasUpgradeModuleUpgrade()).setSlot(ModulePositionType.IN_PIPE).setPositionInt(0).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
		}
	}

	@Override
	public ModernPacket template() {
		return new GuiOpenChassis(getId());
	}
}
