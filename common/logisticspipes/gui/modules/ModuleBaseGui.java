package logisticspipes.gui.modules;

import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.GuiOpenChassie;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

import net.minecraft.inventory.Container;

import lombok.Getter;

public abstract class ModuleBaseGui extends LogisticsBaseGuiScreen {

	@Getter
	protected LogisticsGuiModule module;

	public ModuleBaseGui(Container par1Container, LogisticsGuiModule module) {
		super(par1Container);
		this.module = module;
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (module == null) {
			super.keyTyped(c, i);
			return;
		}
		if (i == 1 || c == 'e') {
			super.keyTyped(c, i);
			if (module.getSlot() == ModulePositionType.SLOT) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiOpenChassie.class).setPosX(module.getX()).setPosY(module.getY()).setPosZ(module.getZ()));
			}
		}
	}
}
