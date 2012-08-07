package net.minecraft.src.buildcraft.krapht.gui.orderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.ItemStack;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.gui.popup.GuiDiskPopup;
import net.minecraft.src.buildcraft.krapht.gui.popup.GuiMessagePopup;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsApiaristAnalyser;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMk2;
import net.minecraft.src.krapht.gui.SmallGuiButton;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen.Colors;

public class NormalMk2GuiOrderer extends NormalGuiOrderer {
	
	public PipeItemsRequestLogisticsMk2 pipe;
	private SmallGuiButton Macrobutton;
	
	public NormalMk2GuiOrderer(PipeItemsRequestLogisticsMk2 RequestPipeMK2 ,EntityPlayer entityPlayer) {
		super(RequestPipeMK2, entityPlayer);
		pipe = RequestPipeMK2;
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
				if(!APIProxy.isRemote()) {
					pipe.dropDisk();
				} else {
					this.setSubGui(new GuiMessagePopup("Comming Soon"));
					//TODO implement for SMP
				}
				lastClickedx = -10000000;
				lastClickedy = -10000000;
			}
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
	}
	
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 12) {
			this.setSubGui(new GuiDiskPopup(this));
			this.getSubGui().setSubGui(new GuiMessagePopup("This a WIP"));
		}
	}
	
	public ItemStack getDisk() {
		return pipe.getDisk();
	}
}
