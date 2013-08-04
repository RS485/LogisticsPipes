package logisticspipes.gui.modules;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.GuiBackPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import buildcraft.transport.Pipe;

public abstract class GuiWithPreviousGuiContainer extends KraphtBaseGuiScreen implements IGuiIDHandlerProvider {
	
	private int prevGuiID = -1;
	protected CoreRoutedPipe pipe;
	private GuiScreen prevGui;
	
	public GuiWithPreviousGuiContainer(Container par1Container, CoreRoutedPipe pipe, GuiScreen prevGui) {
		super(par1Container);
		this.prevGui = prevGui;
		if(prevGui instanceof IGuiIDHandlerProvider) {
			this.prevGuiID = ((IGuiIDHandlerProvider)prevGui).getGuiID();
		}
		this.pipe = pipe;
	}
	
	public GuiScreen getprevGui() {
		return prevGui;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if(pipe == null) {
			super.keyTyped(c, i);
			return;
		}
		if (i == 1 || c == 'e') {
			if (prevGuiID != -1) {
				super.keyTyped(c,i);
//TODO 			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.GUI_BACK_PACKET, pipe.getX(), pipe.getY(), pipe.getZ(), prevGuiID + 10000).getPacket());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(GuiBackPacket.class).setInteger(prevGuiID + 10000).setPosX(pipe.container.xCoord).setPosY(pipe.container.yCoord).setPosZ(pipe.container.zCoord));
			} else {
				super.keyTyped(c, i);
			}
		}
	}
}
