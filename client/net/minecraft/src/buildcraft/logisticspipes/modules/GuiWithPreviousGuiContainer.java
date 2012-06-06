package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.transport.Pipe;

public abstract class GuiWithPreviousGuiContainer extends GuiContainer implements IGuiIDHandlerProvider {
	
	private int prevGuiID = -1;
	private Pipe pipe;
	
	public GuiWithPreviousGuiContainer(Container par1Container, Pipe pipe, GuiScreen prevGuiID) {
		super(par1Container);
		if(prevGuiID instanceof IGuiIDHandlerProvider) {
			this.prevGuiID = ((IGuiIDHandlerProvider)prevGuiID).getGuiID();
		}
		if(pipe == null) {
			throw new NullPointerException("A pipe can't be null");
		}
		this.pipe = pipe;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1 || c == 'e') {
			if (prevGuiID != -1) {
				if(!APIProxy.isClient(mc.theWorld)) {
					mc.thePlayer.openGui(mod_LogisticsPipes.instance, prevGuiID, mc.theWorld, pipe.xCoord, pipe.yCoord, pipe.zCoord);
				} else {
					super.keyTyped(c,i);
					CoreProxy.sendToServer(new PacketPipeInteger(NetworkConstants.GUI_BACK_PACKET, pipe.xCoord, pipe.yCoord, pipe.zCoord, prevGuiID).getPacket());
				}
			} else {
				super.keyTyped(c, i);
			}
		}
	}
}
