package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.Container;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import buildcraft.transport.Pipe;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen;

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
				if(!APIProxy.isClient(mc.theWorld)) {
					mc.thePlayer.openGui(mod_LogisticsPipes.instance, prevGuiID + 10000, mc.theWorld, pipe.xCoord, pipe.yCoord, pipe.zCoord);
				} else {
					super.keyTyped(c,i);
					CoreProxy.sendToServer(new PacketPipeInteger(NetworkConstants.GUI_BACK_PACKET, pipe.xCoord, pipe.yCoord, pipe.zCoord, prevGuiID + 10000).getPacket());
				}
			} else {
				super.keyTyped(c, i);
			}
		}
	}
}
