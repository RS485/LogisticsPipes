package logisticspipes.gui.modules;

import java.io.IOException;

import net.minecraft.inventory.Container;

import lombok.Getter;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.GuiOpenChassis;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public abstract class ModuleBaseGui extends LogisticsBaseGuiScreen {

	@Getter
	protected LogisticsModule module;

	public ModuleBaseGui(Container par1Container, LogisticsModule module) {
		super(par1Container);
		this.module = module;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (module == null) {
			super.keyTyped(typedChar, keyCode);
			return;
		}
		if (keyCode == 1 || typedChar == 'e') {
			super.keyTyped(typedChar, keyCode);
			if (module.getSlot() == ModulePositionType.SLOT) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiOpenChassis.class).setBlockPos(module.getBlockPos()));
			}
		}
	}
}
