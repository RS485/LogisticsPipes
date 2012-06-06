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

import net.java.games.input.Mouse;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderItem;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
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
		super(220,200,0,0);
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
			
			maxPage = (int) Math.floor((_allItems.size() - 1)  / 70F);
			if (page > maxPage){
				page = maxPage;
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

		maxPage = (int) Math.floor((_allItems.size() - 1)  / 70F);
		if (page > maxPage){
			page = maxPage;
		}
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
		if (_itemRequester instanceof PipeItemsRequestLogistics){
			controlList.add(new SmallGuiButton(8, left + 10, bottom - 40, 30, 10, "Stats")); // Stats
		}
		controlList.add(new SmallGuiButton(9, right - 45, bottom - 40, 40, 10, "Both"));
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		drawGuiBackGround();
		
		fontRenderer.drawString(_title, left + fontRenderer.getStringWidth(_title) / 2, top + 6, 0x404040);
		String pageString = "Page " + (page + 1) + " / " + (maxPage + 1);
		fontRenderer.drawString(pageString, right - 47 - fontRenderer.getStringWidth(pageString) / 2 , top + 6 , 0x404040);
		
		String StackrequestCount = ""+(requestCount/64) + "+" + (requestCount % 64);
		
		fontRenderer.drawString("Request number", xCenter - fontRenderer.getStringWidth("Request number") / 2, bottom - 34, 0x404040);
		fontRenderer.drawString(requestCount + "", xCenter - fontRenderer.getStringWidth(requestCount+"") / 2, bottom - 24, 0x404040);
		fontRenderer.drawString(StackrequestCount + "", xCenter - fontRenderer.getStringWidth(StackrequestCount+"") / 2, bottom - 14, 0x404040);
		if (core_LogisticsPipes.DEBUG){
			fontRenderer.drawString(i+","+j, 10, 10, 0xFFFFFF);
			fontRenderer.drawString(lastClickedx+","+lastClickedy, 10, 30, 0xFFFFFF);
		}
		
		int ppi = 0;
		int row = 0;
		int column = 0;
		
		int panelxSize = 20;
		int panelySize = 20;

		if (selectedItem != null){
			String friendlyName = selectedItem.getFriendlyName();
			fontRenderer.drawString(friendlyName, xCenter - fontRenderer.getStringWidth(friendlyName) / 2, bottom - 42, 0x404040);
		}
		
		drawRect(left + 6, top + 16, right - 12, bottom - 44, Colors.MiddleGrey);
		
		for(ItemIdentifier item : _allItems) {
			ppi++;
			
			if (ppi <= 70 * page) continue;
			if (ppi > 70 * (page+1)) continue;
			ItemStack st = _availableItems.containsKey(item)? item.makeNormalStack(_availableItems.get(item)) : item.makeNormalStack(0);
			int x = left + 10 + panelxSize * column;
			int y = top + 18 + panelySize * row;

			GL11.glDisable(2896 /*GL_LIGHTING*/);
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
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		clickWasButton = false;
		super.mouseClicked(i, j, k);
		if (!clickWasButton & i > left + 9 & i < right - 9 && j > top + 15 && j < bottom - 42){
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
		clickWasButton = true;
		
		if (guibutton.id == 0 && selectedItem != null){
			if(!ModLoader.getMinecraftInstance().isMultiplayerWorld()) {
				LogisticsRequest request = new LogisticsRequest(selectedItem, requestCount, this._itemRequester);
				LinkedList<String> errors = new LinkedList<String>();
				boolean result = LogisticsManager.Request(request, this._itemRequester.getRouter().getRoutersByCost(), errors, _entityPlayer);
				if (!result){
					for (String error : errors){
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
		}		
	}
	
	private void prevPage(){
		if (page > 0){
			page--;
		}
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		// Any key close GUI
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
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