/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.orderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.config.Configs;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketRequestGuiContent;
import logisticspipes.network.packets.PacketRequestSubmit;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.terraingen.BiomeEvent.GetWaterColor;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class GuiOrderer extends KraphtBaseGuiScreen implements IItemSearch {

	//protected final IRequestItems _itemRequester;
	public final EntityPlayer _entityPlayer;
	protected ItemIdentifierStack selectedItem = null;
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
	protected int lastClickedk = 0;
	
	protected final String _title = "Request items";
	protected boolean clickWasButton = false;
	
	protected int page = 0;
	protected int maxPage = 0;
	
	protected int requestCount = 1;
	protected Object[] tooltip = null;
	
	protected boolean listbyserver = false;

	public final int xCoord;
	public final int yCoord;
	public final int zCoord;
	public int dimension;
	
	public static int dimensioncache;
	public static long cachetime;
	
	public GuiOrderer(int x, int y, int z, int dim, EntityPlayer entityPlayer) {
		super(220,240,0,0);
		xCoord = x;
		yCoord = y;
		zCoord = z;
		if(cachetime + 100 < System.currentTimeMillis()) {
			dimension = dim;
		} else {
			dimension = dimensioncache;
		}
		_entityPlayer = entityPlayer;
		this.inventorySlots = new DummyContainer(entityPlayer.inventory, null);
	}

	public abstract void refreshItems();

	public void handlePacket(PacketRequestGuiContent packet) {
		listbyserver = true;
		_allItems.clear();
		_allItems.addAll(packet._allItems);
		keepLastItemSelected();
	}

	private void keepLastItemSelected() {
		if(selectedItem == null) return;
		int itemindex = 0;
		int panelxSize = 20;
		int panelySize = 20;
		ItemIdentifier selected = selectedItem.getItem();
		for(ItemIdentifierStack itemStack : _allItems) {
			ItemIdentifier item = itemStack.getItem();
			if(!itemSearched(item)) continue;
			if(item.equals(selected)) {
				page = itemindex / 70;
				lastClickedy = guiTop + 18 + panelySize * ((itemindex % 70) / 10);
				lastClickedx = guiLeft + 10 + panelxSize * (itemindex % 10);
				return;
			}
			itemindex++;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, right - 55, bottom - 25, 50,20,"Request")); // Request
		buttonList.add(new SmallGuiButton(1, right - 15, guiTop + 5, 10 ,10 ,">")); // Next page
		buttonList.add(new SmallGuiButton(2, right - 90, guiTop + 5, 10, 10, "<")); // Prev page
		buttonList.add(new SmallGuiButton(10, xCenter - 51, bottom - 15, 26, 10, "---")); // -64
		buttonList.add(new SmallGuiButton(4, xCenter - 51, bottom - 26, 15, 10, "--")); // -10
		buttonList.add(new SmallGuiButton(5, xCenter - 35, bottom - 26, 10, 10, "-")); // -1
		buttonList.add(new SmallGuiButton(6, xCenter + 26, bottom - 26, 10, 10, "+")); // +1
		buttonList.add(new SmallGuiButton(7, xCenter + 38, bottom - 26, 15, 10, "++")); // +10
		buttonList.add(new SmallGuiButton(11, xCenter + 26, bottom - 15, 26, 10, "+++")); // +64
		buttonList.add(new GuiCheckBox(8, guiLeft + 9, bottom - 60, 14, 14, Configs.DISPLAY_POPUP)); // Popup
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		//super.drawScreen(i, j, f);
		//drawDefaultBackground();
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);

		maxPage = (getSearchedItemNumber() - 1) / 70;
		if(maxPage == -1) maxPage = 0;
		if (page > maxPage){
			page = maxPage;
		}
		
		fontRenderer.drawString(_title, guiLeft + fontRenderer.getStringWidth(_title) / 2, guiTop + 6, 0x404040);
		String pageString = "Page " + (page + 1) + " / " + (maxPage + 1);
		fontRenderer.drawString(pageString, right - 47 - fontRenderer.getStringWidth(pageString) / 2 , guiTop + 6 , 0x404040);
		
		if(buttonList.get(9) instanceof GuiCheckBox && ((GuiCheckBox)buttonList.get(9)).getState()) {
			fontRenderer.drawString("Popup", guiLeft + 25 , bottom - 56, 0x404040);
		} else {
			fontRenderer.drawString("Popup", guiLeft + 25 , bottom - 56, 0xA0A0A0);
		}
		
		String StackrequestCount = ""+(requestCount/getStackAmount()) + "+" + (requestCount % getStackAmount());
		
		fontRenderer.drawString(requestCount + "", xCenter - fontRenderer.getStringWidth(requestCount+"") / 2, bottom - 24, 0x404040);
		fontRenderer.drawString(StackrequestCount + "", xCenter - fontRenderer.getStringWidth(StackrequestCount+"") / 2, bottom - 14, 0x404040);
		
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
				if(lastClickedk == 1) {
					searchinput1 = "";
					searchinput2 = "";
				}
			} else {
				editsearch = false;
			}
		}
		
		int ppi = 0;
		
		int panelxSize = 20;
		int panelySize = 20;

		//if (selectedItem != null){
		//	String friendlyName = selectedItem.getFriendlyName();
		//	fontRenderer.drawString(friendlyName, xCenter - fontRenderer.getStringWidth(friendlyName) / 2, bottom - 39, 0x404040);
		//}
		
		tooltip = null;
		
		drawRect(guiLeft + 6, guiTop + 16, right - 12, bottom - 84, Colors.MiddleGrey);
		
		if(!listbyserver) {
			int graphic = ((int)(System.currentTimeMillis() / 250) % 5);
//			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture());
			mc.renderEngine.bindTexture("/gui/icons.png");
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator var9 = Tessellator.instance;
            var9.startDrawingQuads();
            int xPosition = xCenter - 50;
            int yPosition = guiTop + 40;
            var9.addVertexWithUV(xPosition			, yPosition + 100		, zLevel, 0.04	, 0.72 + (graphic * 0.03125));
            var9.addVertexWithUV(xPosition + 100	, yPosition + 100		, zLevel, 0.08	, 0.72 + (graphic * 0.03125));
            var9.addVertexWithUV(xPosition + 100	, yPosition				, zLevel, 0.08	, 0.69 + (graphic * 0.03125));
            var9.addVertexWithUV(xPosition			, yPosition				, zLevel, 0.04	, 0.69 + (graphic * 0.03125));
            var9.draw();
		} else {
			for(ItemIdentifierStack itemStack : _allItems) {
				ItemIdentifier item = itemStack.getItem();
				if(!itemSearched(item)) continue;
				ppi++;
				
				if (ppi <= 70 * page) continue;
				if (ppi > 70 * (page+1)) break;
				int row = ((ppi - 1) % 70) / 10;
				int column = (ppi - 1) % 10;
				ItemStack st = itemStack.unsafeMakeNormalStack();
				int x = guiLeft + 10 + panelxSize * column;
				int y = guiTop + 18 + panelySize * row;
	
				GL11.glDisable(2896 /*GL_LIGHTING*/);
				int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
	            int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
				
            	if (mouseX >= x && mouseX < x + panelxSize &&
						mouseY >= y && mouseY < y + panelySize) {
					drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.Black);
					drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
					
					tooltip = new Object[]{mouseX,mouseY,st};
				}
				
				if (lastClickedx >= x && lastClickedx < x + panelxSize &&
						lastClickedy >= y && lastClickedy < y + panelySize){
					selectedItem = itemStack;
					drawRect(x - 4, y - 2, x + panelxSize - 2, y + panelySize - 2, Colors.Black);
					drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.White);
					drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
					specialItemRendering(item, x, y);
				}
			}
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(_allItems, this, page, guiLeft + 10, guiTop + 18, 10, 70, panelxSize, panelySize, mc, true, false);
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
	}
	
	public abstract void specialItemRendering(ItemIdentifier item, int x, int y);
	
	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		if(super.hasSubGui()) return;
		BasicGuiHelper.displayItemToolTip(tooltip, this, this.zLevel, guiLeft, guiTop);
	}
	
	@Override
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
		if ((!clickWasButton && i >= guiLeft + 10 && i < right - 10 && j >= guiTop + 18 && j < bottom - 63) || editsearch){
			if(!editsearchb) {
				editsearch = false;
			}
			selectedItem = null;
			lastClickedx = i;
			lastClickedy = j;
			lastClickedk = k;
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
		
		if (isShift && !isControl && isShiftPageChange()){
			if (wheel > 0){
				if (!Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL){
					prevPage();
				} else {
					nextPage();
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL){
					nextPage();
				} else {
					prevPage();
				}
			}
		} else if (isShift && !isControl && !isShiftPageChange()){
			if (wheel > 0){
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - (wheel * getAmountChangeMode(4)));
				} else {
					if(requestCount == 1) requestCount-=1;
					requestCount+= wheel * getAmountChangeMode(4);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if(requestCount == 1) requestCount-=1;
					requestCount+= -(wheel * getAmountChangeMode(4));	
				} else {
					requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(4));
				}
			}
		} else if(!isControl) {
			if (wheel > 0){
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - (wheel * getAmountChangeMode(1)));
				} else {
					requestCount+= wheel * getAmountChangeMode(1);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount+= -(wheel * getAmountChangeMode(1));	
				} else {
					requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(1));
				}
			}
		} else if(isControl && !isShift) {
			if (wheel > 0){
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel * getAmountChangeMode(2));
				} else {
					if(requestCount == 1) requestCount-=1;
					requestCount+= wheel * getAmountChangeMode(2);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if(requestCount == 1) requestCount-=1;
					requestCount+= -wheel * getAmountChangeMode(2);	
				} else {
					requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(2));
				}
			}
		} else if(isControl && isShift) {
			if (wheel > 0){
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel * getAmountChangeMode(3));
				} else {
					if(requestCount == 1) requestCount-=1;
					requestCount+= wheel * getAmountChangeMode(3);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if(requestCount == 1) requestCount-=1;
					requestCount+= -wheel * getAmountChangeMode(3);	
				} else {
					requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(3));
				}
			}
		}
		super.handleMouseInputSub();
	}

	public void handleRequestAnswer(ItemMessage itemMessage, boolean error, ISubGuiControler control, EntityPlayer player) {
		List<ItemMessage> list = new ArrayList<ItemMessage>();
		list.add(itemMessage);
		handleRequestAnswer(list, error, control, player);
	}

	public void handleRequestAnswer(List<ItemMessage> items, boolean error, ISubGuiControler control, EntityPlayer player) {
		if (!error){
			ArrayList<String> msg = new ArrayList<String>();
			msg.add("You are missing:");
			for (ItemMessage item : items){
				if(!Configs.DISPLAY_POPUP) {
					player.addChatMessage("Missing: " + item.toString());
				} else {
					msg.add(item.toString());
				}
			}
			if(Configs.DISPLAY_POPUP) {
				control.setSubGui(new GuiRequestPopup(_entityPlayer, msg.toArray()));
			}
		} else {
			if(Configs.DISPLAY_POPUP) {
				if(control.hasSubGui()) {
					ISubGuiControler newcontroller = control;
					while(newcontroller.hasSubGui()) {
						newcontroller = newcontroller.getSubGui();
					}
					newcontroller.setSubGui(new GuiRequestPopup(_entityPlayer, "Request successful!",items.toArray()));
				} else {
					control.setSubGui(new GuiRequestPopup(_entityPlayer, "Request successful!",items.toArray()));
				}
			} else {
				for(ItemMessage item:items) {
					player.addChatMessage("Requested: " + item);
				}
				player.addChatMessage("Request successful!");
			}
		}
	}

	public void handleSimulateAnswer(List<ItemMessage> used, List<ItemMessage> missing, ISubGuiControler control, EntityPlayer player) {
		if(Configs.DISPLAY_POPUP) {
			if(control.hasSubGui()) {
				ISubGuiControler newcontroller = control;
				while(newcontroller.hasSubGui()) {
					newcontroller = newcontroller.getSubGui();
				}
				newcontroller.setSubGui(new GuiRequestPopup(_entityPlayer, "Components: ", used.toArray(), "Missing: ", missing.toArray()));
			} else {
				control.setSubGui(new GuiRequestPopup(_entityPlayer, "Components: ", used.toArray(), "Missing: ", missing.toArray()));
			}
		} else {
			for(ItemMessage item:used) {
				player.addChatMessage("Component: " + item);
			}
			for(ItemMessage item:missing) {
				player.addChatMessage("Missing: " + item);
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(editsearch) {
			editsearchb = false;
		}
		clickWasButton = true;
		
		if (guibutton.id == 0 && selectedItem != null){
			/*if(!CoreProxy.isRemote()) {
				LogisticsRequest request = new LogisticsRequest(selectedItem, requestCount, this._itemRequester);
				LinkedList<ItemMessage> errors = new LinkedList<ItemMessage>();
				boolean result = LogisticsManager.Request(request, this._itemRequester.getRouter().getRoutersByCost(), errors, _entityPlayer);
				if(result) {
					handleRequestAnswer(new ItemMessage(selectedItem,requestCount),result, this, _entityPlayer);
					refreshItems();
				} else {
					handleRequestAnswer(errors,result, this, _entityPlayer);
				}
				refreshItems();
			} else {*/
				MainProxy.sendPacketToServer(new PacketRequestSubmit(xCoord,yCoord,zCoord,dimension,selectedItem.getItem(),requestCount).getPacket());
				refreshItems();
			//}
		} else if (guibutton.id == 1){
			nextPage();
		} else if (guibutton.id == 2) {
			prevPage();
		} else if (guibutton.id == 3) {
			refreshItems();
		} else if (guibutton.id == 10) {
			requestCount = Math.max(1, requestCount - getAmountChangeMode(3));
		} else if (guibutton.id == 4) {
			requestCount = Math.max(1, requestCount - getAmountChangeMode(2));
		} else if (guibutton.id == 5) {
			requestCount = Math.max(1, requestCount - getAmountChangeMode(1));
		} else if (guibutton.id == 6) {
			requestCount+=getAmountChangeMode(1);
		} else if (guibutton.id == 7) {
			if(requestCount == 1) {
				requestCount-=1;
			}
			requestCount+=getAmountChangeMode(2);
		} else if (guibutton.id == 11) {
			if(requestCount == 1) {
				requestCount-=1;
			}
			requestCount+=getAmountChangeMode(3);
		} else if (guibutton.id == 8) {
			GuiCheckBox button = (GuiCheckBox)guibutton;
			Configs.DISPLAY_POPUP = button.change();
			Configs.savePopupState();
		} else if (guibutton.id == 13 && selectedItem != null){
			MainProxy.sendPacketToServer(new PacketRequestSubmit(xCoord,yCoord,zCoord,dimension,selectedItem.getItem(), requestCount, NetworkConstants.REQUEST_COMPONENTS).getPacket());
		}
		
		super.actionPerformed(guibutton);
	}
	
	protected int getAmountChangeMode(int step) {
		if(step == 1) {
			return 1;
		} else if(step == 2) {
			return 10;
		} else {
			return 64;
		}
	}
	
	protected int getStackAmount() {
		return 64;
	}
	
	protected boolean isShiftPageChange() {
		return true;
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
	public void handleKeyboardInputSub() {
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
				if(selectedItem != null && selectedItem.stackSize != 0) {
					requestCount = selectedItem.stackSize;
				}
			} else if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
				requestCount = 1;
				selectedItem = null;
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				lastClickedk = 0;
			}
		}
		super.handleKeyboardInputSub();
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
	public void resetSubGui() {
		super.resetSubGui();
		refreshItems();
	}
	
	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Normal_Orderer_ID;
	}
}