package logisticspipes.gui.modules;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.GuiOpenChassie;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import net.minecraft.inventory.Container;

public abstract class ModuleBaseGui extends LogisticsBaseGuiScreen {
	
	protected CoreRoutedPipe pipe;
	
	public ModuleBaseGui(Container par1Container, CoreRoutedPipe pipe) {
		super(par1Container);
		this.pipe = pipe;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if(pipe == null) {
			super.keyTyped(c, i);
			return;
		}
		if (i == 1 || c == 'e') {
			super.keyTyped(c,i);
			if(pipe instanceof PipeLogisticsChassi) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiOpenChassie.class).setPosX(pipe.container.xCoord).setPosY(pipe.container.yCoord).setPosZ(pipe.container.zCoord));
			}
		}
	}
}
