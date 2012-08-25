package logisticspipes.gui.modules;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketPipeInteger;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.src.Container;
import net.minecraft.src.GuiScreen;
import buildcraft.transport.Pipe;
import cpw.mods.fml.common.network.PacketDispatcher;

public abstract class GuiWithPreviousGuiContainer extends KraphtBaseGuiScreen implements IGuiIDHandlerProvider {
	
	private int prevGuiID = -1;
	protected Pipe pipe;
	private GuiScreen prevGui;
	
	public GuiWithPreviousGuiContainer(Container par1Container, Pipe pipe, GuiScreen prevGui) {
		super(par1Container);
		this.prevGui = prevGui;
		if(prevGui instanceof IGuiIDHandlerProvider) {
			this.prevGuiID = ((IGuiIDHandlerProvider)prevGui).getGuiID();
		}
		if(pipe == null) {
			throw new NullPointerException("A pipe can't be null");
		}
		this.pipe = pipe;
	}
	
	public GuiScreen getprevGui() {
		return prevGui;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1 || c == 'e') {
			if (prevGuiID != -1) {
				super.keyTyped(c,i);
				PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.GUI_BACK_PACKET, pipe.xCoord, pipe.yCoord, pipe.zCoord, prevGuiID + 10000).getPacket());
			} else {
				super.keyTyped(c, i);
			}
		}
	}
}
