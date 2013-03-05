package logisticspipes.gui.modules;

import org.lwjgl.input.Keyboard;

import logisticspipes.config.Configs;
import logisticspipes.modules.ModuleExpressionSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.KraphtBaseGuiScreen.Colors;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.Pipe;

public class GuiExpressionSink extends GuiWithPreviousGuiContainer {

	public ModuleExpressionSink module;
	boolean clickWasButton = false;
	protected String searchinput1 = "";
	protected String searchinput2 = "";
	protected boolean editsearch = false;
	protected boolean editsearchb = false;
	protected boolean displaycursor = true;
	protected long oldSystemTime = 0;
	protected static int searchWidth = 150;
	int lastClickedx = 0;
	int lastClickedy = 0;
	int lastClickedk = 0;
	
	
	
	public GuiExpressionSink(ModuleExpressionSink module, EntityPlayer player, Pipe pipe, GuiScreen prevGui, int slot) {
		super(new DummyContainer(player.inventory, null), pipe, prevGui);

		
		this.module = module;
		
		this.xSize = 200;
		this.ySize = 200;

	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_ExpressionSink_ID;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		controlList.clear();
		controlList.add(new GuiButton(0, guiLeft + 31, bottom - 160, 50,20,"Add")); 
		controlList.add(new GuiButton(1, guiLeft + 121, bottom - 160, 50,20,"Clear"));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0) {
			if (searchinput1+searchinput2 == "" || searchinput1+searchinput2 == null) return;
			if (module.addExpressionToList((searchinput1 + searchinput2).trim().toLowerCase())) {
				searchinput1 = "";
				searchinput2 = "";
			}
		} else if(par1GuiButton.id == 1) {
			module.clearExpressionList();
			searchinput1 = "";
			searchinput2 = "";
		} else {
			super.actionPerformed(par1GuiButton);			
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		
		//search box
		if(editsearch) {
			drawRect(guiLeft + 30, bottom - 180, right - 28, bottom - 163, Colors.Black);
			drawRect(guiLeft + 31, bottom - 179, right - 29, bottom - 164, Colors.White);
		} else {
			drawRect(guiLeft + 31, bottom - 179, right - 29, bottom - 164, Colors.Black);
		}
		drawRect(guiLeft + 32, bottom - 178, right - 30, bottom - 165, Colors.DarkGrey);
		
		fontRenderer.drawString(searchinput1 + searchinput2, guiLeft + 35, bottom - 175, 0xFFFFFF);
		if(editsearch) {
			int linex = guiLeft + 35 + fontRenderer.getStringWidth(searchinput1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, bottom - 177, linex + 1, bottom - 166, Colors.White);
			}
		}
		
		//Click into search
		if(lastClickedx != -10000000 &&	lastClickedy != -10000000) {
			if (lastClickedx >= guiLeft + 32 && lastClickedx < right - 28 && lastClickedy >= bottom - 180 && lastClickedy < bottom - 163){
				editsearch = true;
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				if(lastClickedk == 1) {
					searchinput1 = "";
					searchinput2 = "";
				}
			} else {
				editsearch = false;
			}
		}
		
		drawRect(guiLeft + 15, bottom - 120, right - 15, bottom - 20, Colors.MiddleGrey);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString("ExpressionSink", guiLeft -50 , bottom - 212, 0x404040);
		fontRenderer.drawString("Included Expressions", 15 , 70, 0x404040);
		
		//add names to list
		for (int i = 0; i < module.expressionList.size(); i++) {
			int x = 20;
			int y = 85+(10*i);
			fontRenderer.drawString(module.expressionList.get(i), x, y, 0x404040);
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		clickWasButton = false;
		editsearchb = true;
		super.mouseClicked(i, j, k);
		if ((!clickWasButton && i >= guiLeft + 10 && i < right - 10 && j >= guiTop + 18 && j < bottom - 63) || editsearch){
			if(!editsearchb) {
				editsearch = false;
			}
			lastClickedx = i;
			lastClickedy = j;
			lastClickedk = k;
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if(editsearch) {
			if (c == 8) {
				if (searchinput1.length() > 0)
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (fontRenderer.getStringWidth(searchinput1 + c + searchinput2) <= getSearchWidth()) {
					searchinput1 += c;
				}
				return;
			} else if(i == 203) { //Left
				if(searchinput1.length() > 0) {
					searchinput2 = searchinput1.substring(searchinput1.length() - 1) + searchinput2;
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				}
			} else if(i == 205) { //Right
				if(searchinput2.length() > 0) {
					searchinput1 += searchinput2.substring(0,1);
					searchinput2 = searchinput2.substring(1);
				}
			} else if(i == 1) { //ESC
				editsearch = false;
			} else if(i == 28) { //Enter
				actionPerformed((GuiButton) controlList.get(0));
			} else if(i == 199) { //Pos
				searchinput2 = searchinput1 + searchinput2;
				searchinput1 = "";
			} else if(i == 207) { //Ende
				searchinput1 = searchinput1 + searchinput2;
				searchinput2 = "";
			} else if(i == 211) { //Entf
				if (searchinput2.length() > 0)
					searchinput2 = searchinput2.substring(1);
			}
		} else {
			super.keyTyped(c, i);
		}
	}
	
	private int getSearchWidth() {
		return 132;
	}

	private String getSearchValue() {
		return searchinput1 + searchinput2;
	}


}
