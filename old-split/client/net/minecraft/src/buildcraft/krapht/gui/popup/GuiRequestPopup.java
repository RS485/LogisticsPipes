package net.minecraft.src.buildcraft.krapht.gui.popup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.krapht.gui.BasicGuiHelper;
import net.minecraft.src.krapht.gui.ISubGuiControler;
import net.minecraft.src.krapht.gui.SubGuiScreen;

public class GuiRequestPopup extends SubGuiScreen {
	
	private String[] text;
	private int mWidth = 0;
	private EntityPlayer player;
	
	public GuiRequestPopup(EntityPlayer player, Object... message) {
		super(200, (message.length * 10) + 40, 0, 0);
		List<String> textArray = new ArrayList<String>();
		for(Object o:message) {
			if(o instanceof Object[]) {
				for(Object oZwei:(Object[])o) {
					textArray.add(oZwei.toString());
				}
			} else {
				textArray.add(o.toString());
			}
		}
		text = textArray.toArray(new String[]{});
		this.ySize = (text.length * 10) + 40;
		this.player = player;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		controlList.clear();
		controlList.add(new GuiButton(0, xCenter - 55, bottom - 25, 50,20,"OK"));
		controlList.add(new GuiButton(1, xCenter + 5, bottom - 25, 50,20,"Log"));
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		if(mWidth == 0) {
			int lWidth = 0;
			for(String msg:text) {
				int tWidth = this.fontRenderer.getStringWidth(msg);
				if(tWidth > lWidth) {
					lWidth = tWidth;
				}
			}
			xSize = mWidth = Math.max(Math.min(lWidth + 20,400),120);
			super.initGui();
		}
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel);
		for(int i=0;i < 9 && i < this.text.length;i++) {
			if(this.text[i] == null) continue;
			String msg = BasicGuiHelper.getCuttedString(this.text[i], mWidth - 10, this.fontRenderer);
			int stringWidth = this.fontRenderer.getStringWidth(msg);
			this.fontRenderer.drawString(msg, xCenter - (stringWidth / 2), guiTop + 10 + (i * 10), 0x404040);
		}
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id) {
		case 0:
			super.exitGui();
			break;
		case 1:
			for(String msg:text) {
				player.addChatMessage(msg);
			}
			((GuiButton)controlList.get(1)).enabled = false;
			break;
		}
	}
}
