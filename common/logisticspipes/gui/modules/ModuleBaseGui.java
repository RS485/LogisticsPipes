package logisticspipes.gui.modules;

import java.io.IOException;

import net.minecraft.inventory.Container;

import lombok.Getter;

import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.GuiOpenChassie;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public abstract class ModuleBaseGui extends LogisticsBaseGuiScreen {

	@Getter
	protected LogisticsGuiModule module;

	public ModuleBaseGui(Container par1Container, LogisticsGuiModule module) {
		super(par1Container);
		this.module = module;
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (module == null) {
			super.keyTyped(c, i);
			return;
		}
		if (i == 1 || c == 'e') {
			super.keyTyped(c, i);
			if (module.getSlot() == ModulePositionType.SLOT) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiOpenChassie.class).setBlockPos(module.getBlockPos()));
			}
		}
	}
}
