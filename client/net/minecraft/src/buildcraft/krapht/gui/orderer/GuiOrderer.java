/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.gui.orderer;

import java.io.Console;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderItem;
import net.minecraft.src.Tessellator;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.core.CoreProxy;
import buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.ItemMessage;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.gui.popup.GuiRequestPopup;
import net.minecraft.src.buildcraft.krapht.network.LogisticsPipesPacket;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketCoordinates;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestGuiContent;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestSubmit;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;
import net.minecraft.src.buildcraft.logisticspipes.statistics.GuiStatistics;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.ItemIdentifierStack;
import net.minecraft.src.krapht.gui.BasicGuiHelper;
import net.minecraft.src.krapht.gui.GuiCheckBox;
import net.minecraft.src.krapht.gui.IItemSearch;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen;
import net.minecraft.src.krapht.gui.SmallGuiButton;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.omg.CORBA._PolicyStub;

public abstract class GuiOrderer extends KraphtBaseGuiScreen implements IItemSearch {

	protected final IRequestItems _itemRequester;
	public final EntityPlayer _entityPlayer;
	protected ItemIdentifier selectedItem = null;
	public final LinkedList<ItemIdentifierStack>_allItems = new LinkedList<ItemIdentifierStack>(); 
	protected String searchinput1 = "";
	protected String searchinput2 = "";
	protected boolean editsearch = false;
	protected boolean editsearchb = false;
	protected boolean displaycursor = true;
	protected long oldSystemTime = 0;
	protected static int searchWidth = 150;
	
	protected RenderItem renderItem = new RenderItem();
	
	protected int lastClickedx = 0;
	protected int lastClickedy = 0;
	
	protected final String _title = "Request items";
	protected boolean clickWasButton = false;
	
	protected int page = 0;
	protected int maxPage = 0;
	
	protected int requestCount = 1;
	protected Object[] tooltip = null;
	
	protected boolean listbyserver = false;
	
	public GuiOrderer(IRequestItems itemRequester, EntityPlayer entityPlayer) {
		super(220,240,0,0);
		_itemRequester = itemRequester;
		_entityPlayer = entityPlayer;
	}

	protected abstract void refreshItems();

	public void handlePacket(PacketRequestGuiContent packet) {
		listbyserver = true;
		_allItems.clear();
		_allItems.addAll(packet._allItems);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		controlList.clear();
		controlList.add(new GuiButton(0, right - 55, bottom - 25, 50,20,"Request")); // Request
		controlList.add(new SmallGuiButton(1, right - 15, guiTop + 5, 10 ,10 ,">")); // Next page
		controlList.add(new SmallGuiButton(2, right - 90, guiTop + 5, 10, 10, "<")); // Prev page
		controlList.add(new GuiButton(3, guiLeft + 10, bottom - 25, 46, 20, "Refresh")); // Refresh
		controlList.add(new SmallGuiButton(10, xCenter - 41, bottom - 15, 26, 10, "---")); // -64
		controlList.add(new SmallGuiButton(4, xCenter - 41, bottom - 26, 15, 10, "--")); // -10
		controlList.add(new SmallGuiButton(5, xCenter - 25, bottom - 26, 10, 10, "-")); // -1
		controlList.add(new SmallGuiButton(6, xCenter + 16, bottom - 26, 10, 10, "+")); // +1
		controlList.add(new SmallGuiButton(7, xCenter + 28, bottom - 26, 15, 10, "++")); // +10
		controlList.add(new SmallGuiButton(11, xCenter + 16, bottom - 15, 26, 10, "+++")); // +64
		controlList.add(new GuiCheckBox(8, guiLeft + 9, bottom - 60, 14, 14, mod_LogisticsPipes.displayPopup)); // Popup
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		//super.drawScreen(i, j, f);
		//drawDefaultBackground();
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel);

		maxPage = (int) Math.floor((getSearchedItemNumber() - 1)  / 70F);
		if(maxPage == -1) maxPage = 0;
		if (page > maxPage){
			page = maxPage;
		}
		
		fontRenderer.drawString(_title, guiLeft + fontRenderer.getStringWidth(_title) / 2, guiTop + 6, 0x404040);
		String pageString = "Page " + (page + 1) + " / " + (maxPage + 1);
		fontRenderer.drawString(pageString, right - 47 - fontRenderer.getStringWidth(pageString) / 2 , guiTop + 6 , 0x404040);
		
		if(controlList.get(10) instanceof GuiCheckBox && ((GuiCheckBox)controlList.get(10)).getState()) {
			fontRenderer.drawString("Popup", guiLeft + 25 , bottom - 56, 0x404040);
		} else {
			fontRenderer.drawString("Popup", guiLeft + 25 , bottom - 56, 0xA0A0A0);
		}
		
		String StackrequestCount = ""+(requestCount/64) + "+" + (requestCount % 64);
		
		fontRenderer.drawString(requestCount + "", xCenter - fontRenderer.getStringWidth(requestCount+"") / 2, bottom - 24, 0x404040);
		fontRenderer.drawString(StackrequestCount + "", xCenter - fontRenderer.getStringWidth(StackrequestCount+"") / 2, bottom - 14, 0x404040);
		if (core_LogisticsPipes.DEBUG){
			fontRenderer.drawString(i+","+j, 10, 10, 0xFFFFFF);
			fontRenderer.drawString(lastClickedx+","+lastClickedy, 10, 30, 0xFFFFFF);
		}
		
		//SearchInput
		if(editsearch) {
			drawRect(guiLeft + 30, bottom - 80, right - 28, bottom - 63, Colors.Black);
			drawRect(guiLeft + 31, bottom - 79, right - 29, bottom - 64, Colors.White);
		} else {
			drawRect(guiLeft + 31, bottom - 79, right - 29, bottom - 64, Colors.Black);
		}
		drawRect(guiLeft + 32, bottom - 78, right - 30, bottom - 65, Colors.DarkGrey);
		
		fontRenderer.drawString(searchinput1 + searchinput2, guiLeft + 35, bottom - 75, 0xFFFFFF);
		if(editsearch) {
			int linex = guiLeft + 35 + fontRenderer.getStringWidth(searchinput1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, bottom - 77, linex + 1, bottom - 66, Colors.White);
			}
		}
		
		//Click into search
		if(lastClickedx != -10000000 &&	lastClickedy != -10000000) {
			if (lastClickedx >= guiLeft + 32 && lastClickedx < right - 28 &&
					lastClickedy >= bottom - 80 && lastClickedy < bottom - 63){
				editsearch = true;
				lastClickedx = -10000000;
				lastClickedy = -10000000;
			} else {
				editsearch = false;
			}
		}
		
		int ppi = 0;
		int row = 0;
		int column = 0;
		
		int panelxSize = 20;
		int panelySize = 20;

		//if (selectedItem != null){
		//	String friendlyName = selectedItem.getFriendlyName();
		//	fontRenderer.drawString(friendlyName, xCenter - fontRenderer.getStringWidth(friendlyName) / 2, bottom - 39, 0x404040);
		//}
		
		tooltip = null;
		
		drawRect(guiLeft + 6, guiTop + 16, right - 12, bottom - 84, Colors.MiddleGrey);
		
		if(!listbyserver && APIProxy.isRemote()) {
			int graphic = ((int)(System.currentTimeMillis() / 250) % 5);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/icons.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator var9 = Tessellator.instance;
            var9.startDrawingQuads();
            int xPosition = xCenter - 50;
            int yPosition = guiTop + 40;
            var9.addVertexWithUV(xPosition			, yPosition + 100		, (double)zLevel, 0.04	, 0.72 + (graphic * 0.03125));
            var9.addVertexWithUV(xPosition + 100	, yPosition + 100		, (double)zLevel, 0.08	, 0.72 + (graphic * 0.03125));
            var9.addVertexWithUV(xPosition + 100	, yPosition				, (double)zLevel, 0.08	, 0.69 + (graphic * 0.03125));
            var9.addVertexWithUV(xPosition			, yPosition				, (double)zLevel, 0.04	, 0.69 + (graphic * 0.03125));
            var9.draw();
		} else {
			long starttime = System.currentTimeMillis();
			for(ItemIdentifierStack itemStack : _allItems) {
				ItemIdentifier item = itemStack.getItem();
				if(!itemSearched(item)) continue;
				ppi++;
				
				if (ppi <= 70 * page) continue;
				if (ppi > 70 * (page+1)) continue;
				ItemStack st = itemStack.makeNormalStack();
				int x = guiLeft + 10 + panelxSize * column;
				int y = guiTop + 18 + panelySize * row;
	
				GL11.glDisable(2896 /*GL_LIGHTING*/);
				int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
	            int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
				
	            if(!super.hasSubGui()) {
					if (mouseX >= x && mouseX < x + panelxSize &&
							mouseY >= y && mouseY < y + panelySize) {
						drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.Black);
						drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
						
						tooltip = new Object[]{mouseX,mouseY,st};
					}
					
					if (lastClickedx >= x && lastClickedx < x + panelxSize &&
							lastClickedy >= y && lastClickedy < y + panelySize){
						selectedItem = item;
						drawRect(x - 4, y - 2, x + panelxSize - 2, y + panelySize - 2, Colors.Black);
						drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.White);
						drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
					}
	            }
				/*
				renderItem.renderItemIntoGUI(fontRenderer, mc.renderEngine, st, x, y);
				String s;
				if (st.stackSize == 1){
					s = "";
				} else if (st.stackSize < 1000) {
					s = st.stackSize + "";
				} else if (st.stackSize < 1000000){
					s = st.stackSize / 1000 + "K";
				} else {
					s = st.stackSize / 1000000 + "M";
				}
					
				GL11.glDisable(2896 /*GL_LIGHTING* /);
				GL11.glDisable(2929 /*GL_DEPTH_TEST* /);			
				fontRenderer.drawStringWithShadow(s, x + 16 - fontRenderer.getStringWidth(s), y + 8, 0xFFFFFF);
	            GL11.glEnable(2929 /*GL_DEPTH_TEST* /);
				GL11.glEnable(2896 /*GL_LIGHTING* /);
				*/
				column++;
				if (column == 10){
					row++;
					column = 0;
				}
			}
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(_allItems, this, page, guiLeft + 10, guiTop + 18, 10, 70, panelxSize, panelySize, mc, true, false);
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
	}
	
	@Override
	public void drawGuiContainerForegroundLayer() {
		if(super.hasSubGui()) return;
		BasicGuiHelper.displayItemToolTip(tooltip, this, this.zLevel, guiLeft, guiTop);
	}
	
	public boolean itemSearched(ItemIdentifier item) {
		if(searchinput1 == "" && searchinput2 == "") return true;
		if(isSearched(item.getFriendlyName().toLowerCase(),(searchinput1 + searchinput2).toLowerCase())) return true;
		if(isSearched(String.valueOf(item.itemID),(searchinput1 + searchinput2))) return true;
		return false;
	}
	
	private boolean isSearched(String value, String search) {
		boolean flag = true;
		for(String s:search.split(" ")) {
			if(!value.contains(s)) {
				flag = false;
			}
		}
		return flag;
	}
	
	private int getSearchedItemNumber() {
		int count = 0;
		for(ItemIdentifierStack item : _allItems) {
			if(itemSearched(item.getItem())) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		clickWasButton = false;
		editsearchb = true;
		super.mouseClicked(i, j, k);
		if ((!clickWasButton & i > guiLeft & i < right && j > guiTop && j < bottom) || editsearch){
			if(!editsearchb) {
				editsearch = false;
			}
			selectedItem = null;
			lastClickedx = i;
			lastClickedy = j;
		}
	}
	
	@Override
	public void handleMouseInputSub() {
		boolean isShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		boolean isControl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0){
			super.handleMouseInputSub();
			return;
		}
		
		if (isShift && !isControl){
			if (wheel > 0){
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_PAGE_INVERTWHEEL){
					prevPage();
				} else {
					nextPage();
				}
			} else {
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_PAGE_INVERTWHEEL){
					nextPage();
				} else {
					prevPage();
				}
			}
		} else if(!isControl) {
			if (wheel > 0){
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel);
				} else {
					requestCount+= wheel;
				}
			} else {
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount+= -wheel;	
				} else {
					requestCount = Math.max(1, requestCount + wheel);
				}
			}
		} else if(isControl && !isShift) {
			if (wheel > 0){
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel*10);
				} else {
					if(requestCount == 1) requestCount-=1;
					requestCount+= wheel*10;
				}
			} else {
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if(requestCount == 1) requestCount-=1;
					requestCount+= -wheel*10;	
				} else {
					requestCount = Math.max(1, requestCount + wheel*10);
				}
			}
		} else if(isControl && isShift) {
			if (wheel > 0){
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel*64);
				} else {
					if(requestCount == 1) requestCount-=1;
					requestCount+= wheel*64;
				}
			} else {
				if (!core_LogisticsPipes.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if(requestCount == 1) requestCount-=1;
					requestCount+= -wheel*64;	
				} else {
					requestCount = Math.max(1, requestCount + wheel*64);
				}
			}
		}
		super.handleMouseInputSub();
	}

	public void handleRequestAnswer(ItemMessage itemMessage, boolean error) {
		List<ItemMessage> list = new ArrayList<ItemMessage>();
		list.add(itemMessage);
		handleRequestAnswer(list, error);
	}

	public void handleRequestAnswer(List<ItemMessage> items, boolean error) {
		if (!error){
			ArrayList<String> msg = new ArrayList<String>();
			msg.add("You are missing:");
			for (ItemMessage item : items){
				if(!mod_LogisticsPipes.displayPopup) {
					_entityPlayer.addChatMessage("Missing: " + item.toString());
				} else {
					msg.add(item.toString());
				}
			}
			if(mod_LogisticsPipes.displayPopup) {
				this.setSubGui(new GuiRequestPopup(_entityPlayer, msg.toArray()));
			}
		}
		else{
			if(mod_LogisticsPipes.displayPopup) {
				this.setSubGui(new GuiRequestPopup(_entityPlayer, "Request successful!",items.toArray()));	
			} else {
				for(ItemMessage item:items) {
					_entityPlayer.addChatMessage("Requested: " + item);
				}
				_entityPlayer.addChatMessage("Request successful!");
			}
			refreshItems();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(editsearch) {
			editsearchb = false;
		}
		clickWasButton = true;
		
		if (guibutton.id == 0 && selectedItem != null){
			if(!ModLoader.getMinecraftInstance().isMultiplayerWorld()) {
				LogisticsRequest request = new LogisticsRequest(selectedItem, requestCount, this._itemRequester);
				LinkedList<ItemMessage> errors = new LinkedList<ItemMessage>();
				boolean result = LogisticsManager.Request(request, this._itemRequester.getRouter().getRoutersByCost(), errors, _entityPlayer);
				if(result) {
					handleRequestAnswer(new ItemMessage(selectedItem,requestCount),result);
				} else {
					handleRequestAnswer(errors,result);
				}
				refreshItems();
			} else {
				CoreRoutedPipe requestPipe = (CoreRoutedPipe)_itemRequester;
				CoreProxy.sendToServer(new PacketRequestSubmit(requestPipe.xCoord,requestPipe.yCoord,requestPipe.zCoord,selectedItem,requestCount).getPacket());
				refreshItems();
			}
		} else if (guibutton.id == 1){
			nextPage();
		} else if (guibutton.id == 2) {
			prevPage();
		} else if (guibutton.id == 3) {
			refreshItems();
		} else if (guibutton.id == 10) {
			requestCount = Math.max(1, requestCount - 64);
		} else if (guibutton.id == 4) {
			requestCount = Math.max(1, requestCount - 10);
		} else if (guibutton.id == 5) {
			requestCount = Math.max(1, requestCount - 1);
		} else if (guibutton.id == 6) {
			requestCount+=1;
		} else if (guibutton.id == 7) {
			if(requestCount == 1) {
				requestCount-=1;
			}
			requestCount+=10;
		} else if (guibutton.id == 11) {
			if(requestCount == 1) {
				requestCount-=1;
			}
			requestCount+=64;
		} else if (guibutton.id == 8) {
			GuiCheckBox button = (GuiCheckBox)controlList.get(10);
			mod_LogisticsPipes.displayPopup = button.change();
		}
		
		super.actionPerformed(guibutton);
	}
	
	private void nextPage(){
		if (page < maxPage){
			page++;
		} else {
			page = 0;
		}
	}
	
	private void prevPage(){
		if (page > 0){
			page--;
		} else {
			page = maxPage;
		}
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if(editsearch) {
			if (c == 13) {
				editsearch = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				searchinput1 = searchinput1 + getClipboardString();
			} else if (c == 8) {
				if (searchinput1.length() > 0)
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (fontRenderer.getStringWidth(searchinput1 + c + searchinput2) <= searchWidth) {
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
				editsearch = false;
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
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
//			super.keyTyped(c, i);
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
//			super.keyTyped(c, i);
		} else {
			super.keyTyped(c, i);
		}
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Normal_Orderer_ID;
	}
}