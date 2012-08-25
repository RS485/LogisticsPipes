package logisticspipes.gui.orderer;

import logisticspipes.gui.popup.GuiDiskPopup;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketCoordinates;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.ItemStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;

public class NormalMk2GuiOrderer extends NormalGuiOrderer {
	
	public PipeItemsRequestLogisticsMk2 pipe;
	private SmallGuiButton Macrobutton;
	
	public NormalMk2GuiOrderer(PipeItemsRequestLogisticsMk2 RequestPipeMK2 ,EntityPlayer entityPlayer) {
		super(RequestPipeMK2, entityPlayer);
		pipe = RequestPipeMK2;
		//if(APIProxy.isRemote()) {
		PacketDispatcher.sendPacketToServer(new PacketCoordinates(NetworkConstants.DISK_REQUEST_CONTENT, pipe.xCoord, pipe.yCoord, pipe.zCoord).getPacket());
		//}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		controlList.add(Macrobutton = new SmallGuiButton(12, right - 55, bottom - 60, 50, 10, "Disk"));
		Macrobutton.enabled = false;
	}
	
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);

		drawRect(right - 39, bottom - 47, right - 19, bottom - 27, Colors.Black);
		drawRect(right - 37, bottom - 45, right - 21, bottom - 29, Colors.DarkGrey);
		
		if(pipe.getDisk() != null) {
			renderItem.renderItemIntoGUI(fontRenderer, mc.renderEngine, pipe.getDisk(), right - 37, bottom - 45);
			Macrobutton.enabled = true;
		} else {
			Macrobutton.enabled = false;
		}
		
		//Click on Disk
		if(lastClickedx != -10000000 &&	lastClickedy != -10000000) {
			if (lastClickedx >= right - 39 && lastClickedx < right - 19 && lastClickedy >= bottom - 47 && lastClickedy < bottom - 27) {
				PacketDispatcher.sendPacketToServer(new PacketCoordinates(NetworkConstants.DISK_DROP, pipe.xCoord, pipe.yCoord, pipe.zCoord).getPacket());
				lastClickedx = -10000000;
				lastClickedy = -10000000;
			}
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
	}
	
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 12) {
			PacketDispatcher.sendPacketToServer(new PacketCoordinates(NetworkConstants.DISK_REQUEST_CONTENT, pipe.xCoord, pipe.yCoord, pipe.zCoord).getPacket());
			this.setSubGui(new GuiDiskPopup(this));
		}
	}
	
	public ItemStack getDisk() {
		return pipe.getDisk();
	}
}
