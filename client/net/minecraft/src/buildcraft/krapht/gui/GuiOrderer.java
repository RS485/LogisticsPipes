/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.gui;

import java.io.Console;
import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.input.Mouse;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderItem;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.ErrorMessage;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketCoordinates;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestGuiContent;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestSubmit;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;
import net.minecraft.src.buildcraft.logisticspipes.statistics.GuiStatistics;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen;
import net.minecraft.src.krapht.gui.SmallGuiButton;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.omg.CORBA._PolicyStub;

public class GuiOrderer extends KraphtBaseGuiScreen {

	private enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly,
	}
	
	private final IRequestItems _itemRequester;
	private final EntityPlayer _entityPlayer;
	private HashMap<ItemIdentifier, Integer> _availableItems;
	private LinkedList<ItemIdentifier> _craftableItems;
	private ItemIdentifier selectedItem = null;
	private final LinkedList<ItemIdentifier>_allItems = new LinkedList<ItemIdentifier>(); 
	private String searchinput1 = "";
	private String searchinput2 = "";
	private boolean editsearch = false;
	private boolean editsearchb = false;
	private boolean displaycursor = true;
	private long oldSystemTime = 0;
	private static int searchWidth = 150;
	
	private RenderItem renderItem = new RenderItem();
	
	private int lastClickedx = 0;
	private int lastClickedy = 0;
	
	private final String _title = "Request items";
	private boolean clickWasButton = false;
	
	private int page = 0;
	private int maxPage = 0;
	
	private int requestCount = 1;
	
	private DisplayOptions displayOptions = DisplayOptions.Both;
	
	public GuiOrderer(IRequestItems itemRequester, EntityPlayer entityPlayer) {
		super(220,220,0,0);
		_itemRequester = itemRequester;
		_entityPlayer = entityPlayer;
		refreshItems();
	}
	
	private void refreshItems(){
		if(!ModLoader.getMinecraftInstance().isMultiplayerWorld()) {
			if (displayOptions == DisplayOptions.SupplyOnly || displayOptions == DisplayOptions.Both){
				_availableItems = core_LogisticsPipes.logisticsManager.getAvailableItems(_itemRequester.getRouter().getRouteTable().keySet());
			} else {
				_availableItems = new HashMap<ItemIdentifier, Integer>();
			}
			if (displayOptions == DisplayOptions.CraftOnly || displayOptions == DisplayOptions.Both){
				_craftableItems = core_LogisticsPipes.logisticsManager.getCraftableItems(_itemRequester.getRouter().getRouteTable().keySet());
			} else {
				_craftableItems = new LinkedList<ItemIdentifier>();
			}
			_allItems.clear();
			
			outer:
			for (ItemIdentifier item : _availableItems.keySet()){
				for (int i = 0; i <_allItems.size(); i++){
					if (item.itemID < _allItems.get(i).itemID || item.itemID == _allItems.get(i).itemID && item.itemDamage < _allItems.get(i).itemDamage){
						_allItems.add(i, item);
						continue outer;
					}
				}
				_allItems.addLast(item);
			}
			
			outer:
			for (ItemIdentifier item : _craftableItems){
				if (_allItems.contains(item)) continue;
				for (int i = 0; i <_allItems.size(); i++){
					if (item.itemID < _allItems.get(i).itemID || item.itemID == _allItems.get(i).itemID && item.itemDamage < _allItems.get(i).itemDamage){
						_allItems.add(i, item);
						continue outer;
					}
				}
				_allItems.addLast(item);
			}
		} else {
			CoreRoutedPipe requestPipe = (CoreRoutedPipe)_itemRequester;
			int integer;
			switch(displayOptions) {
			case Both:
				integer = 0;
				break;
			case SupplyOnly:
				integer = 1;
				break;
			case CraftOnly:
				integer = 2;
				break;
			default: 
				integer = 3;
			}
			CoreProxy.sendToServer(new PacketPipeInteger(NetworkConstants.ORDERER_REFRESH_REQUEST,requestPipe.xCoord,requestPipe.yCoord,requestPipe.zCoord,integer).getPacket());
		}
	}
	
	public void handlePacket(PacketRequestGuiContent packet) {
		_availableItems = packet._availableItems;
		_craftableItems = packet._craftableItems;
		_allItems.clear();
		_allItems.addAll(packet._allItems);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		controlList.add(new GuiButton(0, right - 55, bottom - 25, 50,20,"Request")); // Request
		controlList.add(new SmallGuiButton(1, right - 15, top + 5, 10 ,10 ,">")); // Next page
		controlList.add(new SmallGuiButton(2, right - 90, top + 5, 10, 10, "<")); // Prev page
		controlList.add(new GuiButton(3, left + 10, bottom - 25, 46, 20, "Refresh")); // Refresh
		controlList.add(new SmallGuiButton(10, xCenter - 41, bottom - 15, 26, 10, "---")); // -64
		controlList.add(new SmallGuiButton(4, xCenter - 41, bottom - 26, 15, 10, "--")); // -10
		controlList.add(new SmallGuiButton(5, xCenter - 25, bottom - 26, 10, 10, "-")); // -1
		controlList.add(new SmallGuiButton(6, xCenter + 16, bottom - 26, 10, 10, "+")); // +1
		controlList.add(new SmallGuiButton(7, xCenter + 28, bottom - 26, 15, 10, "++")); // +10
		controlList.add(new SmallGuiButton(11, xCenter + 16, bottom - 15, 26, 10, "+++")); // +64
		//if (_itemRequester instanceof PipeItemsRequestLogistics){
			//controlList.add(new SmallGuiButton(8, left + 10, bottom - 40, 30, 10, "Stats")); // Stats
		//}
		controlList.add(new SmallGuiButton(9, right - 45, bottom - 40, 40, 10, "Both"));
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		drawDefaultBackground();
		drawGuiBackGround();

		maxPage = (int) Math.floor((getSearchedItemNumber() - 1)  / 70F);
		if(maxPage == -1) maxPage = 0;
		if (page > maxPage){
			page = maxPage;
		}
		
		fontRenderer.drawString(_title, left + fontRenderer.getStringWidth(_title) / 2, top + 6, 0x404040);
		String pageString = "Page " + (page + 1) + " / " + (maxPage + 1);
		fontRenderer.drawString(pageString, right - 47 - fontRenderer.getStringWidth(pageString) / 2 , top + 6 , 0x404040);
		
		String StackrequestCount = ""+(requestCount/64) + "+" + (requestCount % 64);
		
		//fontRenderer.drawString("Request number", xCenter - fontRenderer.getStringWidth("Request number") / 2, bottom - 34, 0x404040);
		fontRenderer.drawString(requestCount + "", xCenter - fontRenderer.getStringWidth(requestCount+"") / 2, bottom - 24, 0x404040);
		fontRenderer.drawString(StackrequestCount + "", xCenter - fontRenderer.getStringWidth(StackrequestCount+"") / 2, bottom - 14, 0x404040);
		if (core_LogisticsPipes.DEBUG){
			fontRenderer.drawString(i+","+j, 10, 10, 0xFFFFFF);
			fontRenderer.drawString(lastClickedx+","+lastClickedy, 10, 30, 0xFFFFFF);
		}
		
		//SearchInput
		if(editsearch) {
			drawRect(left + 30, bottom - 60, right - 28, bottom - 43, Colors.Black);
			drawRect(left + 31, bottom - 59, right - 29, bottom - 44, Colors.White);
		} else {
			drawRect(left + 31, bottom - 59, right - 29, bottom - 44, Colors.Black);
		}
		drawRect(left + 32, bottom - 58, right - 30, bottom - 45, Colors.DarkGrey);
		
		fontRenderer.drawString(searchinput1 + searchinput2, left + 35, bottom - 55, 0xFFFFFF);
		if(editsearch) {
			int linex = left + 35 + fontRenderer.getStringWidth(searchinput1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, bottom - 57, linex + 1, bottom - 46, Colors.White);
			}
		}
		
		if(lastClickedx != -10000000 &&	lastClickedy != -10000000) {
			if (lastClickedx >= left + 32 && lastClickedx < right - 28 &&
					lastClickedy >= bottom - 60 && lastClickedy < bottom - 43){
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

		if (selectedItem != null){
			String friendlyName = selectedItem.getFriendlyName();
			fontRenderer.drawString(friendlyName, xCenter - fontRenderer.getStringWidth(friendlyName) / 2, bottom - 39, 0x404040);
		}
		
		drawRect(left + 6, top + 16, right - 12, bottom - 64, Colors.MiddleGrey);
		
		for(ItemIdentifier item : _allItems) {
			if(!itemSearched(item)) continue;
			ppi++;
			
			if (ppi <= 70 * page) continue;
			if (ppi > 70 * (page+1)) continue;
			ItemStack st = _availableItems.containsKey(item)? item.makeNormalStack(_availableItems.get(item)) : item.makeNormalStack(0);
			int x = left + 10 + panelxSize * column;
			int y = top + 18 + panelySize * row;

			GL11.glDisable(2896 /*GL_LIGHTING*/);
			int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
			
			if (mouseX >= x && mouseX < x + panelxSize &&
					mouseY >= y && mouseY < y + panelySize) {
				drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.Black);
				drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
			}
			
			if (lastClickedx >= x && lastClickedx < x + panelxSize &&
					lastClickedy >= y && lastClickedy < y + panelySize){
				selectedItem = item;
				drawRect(x - 4, y - 2, x + panelxSize - 2, y + panelySize - 2, Colors.Black);
				drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.White);
				drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
			}
			
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
				
			GL11.glDisable(2896 /*GL_LIGHTING*/);
			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);			
			fontRenderer.drawStringWithShadow(s, x + 16 - fontRenderer.getStringWidth(s), y + 8, 0xFFFFFF);
            GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
			GL11.glEnable(2896 /*GL_LIGHTING*/);

			column++;
			if (column == 10){
				row++;
				column = 0;
			}
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		super.drawScreen(i, j, f);
	}
	
	private boolean itemSearched(ItemIdentifier item) {
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
		for(ItemIdentifier item : _allItems) {
			if(itemSearched(item)) {
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
		if ((!clickWasButton & i > left + 9 & i < right - 9 && j > top + 15 && j < bottom - 42) || editsearch){
			if(!editsearchb) {
				editsearch = false;
			}
			selectedItem = null;
			lastClickedx = i;
			lastClickedy = j;
		}
	}
	
	@Override
	public void handleMouseInput() {
		boolean isShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		boolean isControl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0){
			super.handleMouseInput();
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
		super.handleMouseInput();
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
				LinkedList<ErrorMessage> errors = new LinkedList<ErrorMessage>();
				boolean result = LogisticsManager.Request(request, this._itemRequester.getRouter().getRoutersByCost(), errors, _entityPlayer);
				if (!result){
					for (ErrorMessage error : errors){
						_entityPlayer.addChatMessage("Missing: " + error);
					}
				}
				else{
					_entityPlayer.addChatMessage("Request successful!");
					refreshItems();
				}
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
		} else if (guibutton.id == 8 && selectedItem != null) {
			//if(!ModLoader.getMinecraftInstance().isMultiplayerWorld()) {
				PipeItemsRequestLogistics requestPipe = (PipeItemsRequestLogistics)_itemRequester;
				//_entityPlayer.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_OrdererStats_ID, requestPipe.worldObj, requestPipe.xCoord, requestPipe.yCoord, requestPipe.zCoord);
				ModLoader.openGUI(_entityPlayer, new GuiStatistics(requestPipe.getHistory(), selectedItem, this, _entityPlayer, requestPipe));
			//} else {
				//TODo send To Server
			//}
		} else if (guibutton.id == 9) {
			String displayString = "";
			switch (displayOptions){
			case Both:
				displayOptions = DisplayOptions.CraftOnly;
				displayString = "Craft";
				break;
			case CraftOnly:
				displayOptions = DisplayOptions.SupplyOnly;
				displayString = "Supply";
				break;
			case SupplyOnly:
				displayOptions = DisplayOptions.Both;
				displayString = "Both";
				break;
			}
			guibutton.displayString = displayString;
			refreshItems();
			
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
		} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
			super.keyTyped(c, i);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
			super.keyTyped(c, i);
		} else {
			super.keyTyped(c, 1);
		}
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Orderer_ID;
	}
}