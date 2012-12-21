package logisticspipes.gui.popup;

import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.SubGuiScreen;
import net.minecraft.client.gui.GuiButton;

public class GuiMessagePopup extends SubGuiScreen {

	private String[] text;
	private int mWidth = 0;
	
	public GuiMessagePopup( Object... message) {
		super(200, (message.length * 10) + 40, 0, 0);
		text = new String[message.length];
		int i=0;
		for(Object o:message) {
			if(o instanceof Object[]) {
				for(Object oZwei:(Object[])o) {
					text[i++] = oZwei.toString();
				}
			} else {
				text[i++] = o.toString();
			}
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		controlList.clear();
		controlList.add(new GuiButton(0, xCenter - 25, bottom - 25, 50,20,"OK"));
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
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
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
		}
	}
}
