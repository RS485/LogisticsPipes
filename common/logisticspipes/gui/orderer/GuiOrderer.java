/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.orderer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.config.Configs;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestComponentPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public abstract class GuiOrderer extends KraphtBaseGuiScreen implements IItemSearch {

	//protected final IRequestItems _itemRequester;
	public final EntityPlayer _entityPlayer;
	protected ItemIdentifierStack selectedItem = null;
	public final LinkedList<ItemIdentifierStack>_allItems = new LinkedList<ItemIdentifierStack>(); 
	protected String searchinput1 = "";
	protected String searchinput2 = "";
	protected boolean editsearch = true;
	protected boolean editsearchb = false;
	protected boolean displaycursor = true;
	protected long oldSystemTime = 0;
	protected static int searchWidth = 150;
	
	protected RenderItem renderItem = new RenderItem();
	
	protected int lastClickedx = -10000000;
	protected int lastClickedy = -10000000;
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

	public void handlePacket(Collection<ItemIdentifierStack> allItems) {
		listbyserver = true;
		_allItems.clear();
		_allItems.addAll(allItems);
		Collections.sort(_allItems, new ItemIdentifierStack.orderedComparitor());
		keepLastItemSelected();
	}

	private void keepLastItemSelected() {
		if (selectedItem == null)
			return;
		int itemindex = 0;
		
		// GL11.glTranslatef((float) guiLeft, (float) guiTop, 0.0F);
		// Field: 10, 18, xSize - 10, ySize - 82
		int panelxSize = 20;
		int panelySize = 20;
		
		ItemIdentifier selected = selectedItem.getItem();
		for (ItemIdentifierStack itemStack : _allItems) {
			ItemIdentifier item = itemStack.getItem();
			if (!itemSearched(item))
				continue;
			if (item.equals(selected)) {
				page = itemindex / 70;
				lastClickedx = guiLeft + 10 + (panelxSize * (itemindex % 10));
				lastClickedy = guiTop + 18 + (panelySize * ((itemindex % 70) / 10));
				return;
			}
			itemindex++;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		this.guiLeft =  width/2 - xSize/2 + xCenterOffset;
		this.guiTop = height/2 - ySize/2  + yCenterOffset;
		
		this.right = width/2 + xSize/2 + xCenterOffset;
		this.bottom = height/2 + ySize/2 + yCenterOffset;
		
		this.guiLeft += this.getLeftAddition();
		this.xCenter = (right + guiLeft) / 2;
		this.yCenter = (bottom + guiTop) / 2;
		
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
		this.guiLeft -= this.getLeftAddition();
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	protected int getLeftAddition() {
		return 0;
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/icons.png");
	
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
		
		this.guiLeft += this.getLeftAddition();
		
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

		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft, guiTop, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		drawRect(10, 18, xSize - 10 - this.getLeftAddition(), ySize - 82, Colors.MiddleGrey);

		tooltip = null;
		int ppi = 0;
		int panelxSize = 20;
		int panelySize = 20;
		int x = 12;
		int y = 20;
		int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;

		if (!listbyserver) {
			int graphic = ((int) (System.currentTimeMillis() / 250) % 5);
			// GL11.glBindTexture(GL11.GL_TEXTURE_2D,
			// this.mc.renderEngine.getTexture());
			mc.renderEngine.func_110577_a(TEXTURE);
			Tessellator tesselator = Tessellator.instance;
			tesselator.startDrawingQuads();
			int xPosition = (xSize / 2) - 50;
			int yPosition = 40;
			tesselator.addVertexWithUV(xPosition, yPosition + 100, zLevel, 0.04, 0.72 + (graphic * 0.03125));
			tesselator.addVertexWithUV(xPosition + 100, yPosition + 100, zLevel, 0.08, 0.72 + (graphic * 0.03125));
			tesselator.addVertexWithUV(xPosition + 100, yPosition, zLevel, 0.08, 0.69 + (graphic * 0.03125));
			tesselator.addVertexWithUV(xPosition, yPosition, zLevel, 0.04, 0.69 + (graphic * 0.03125));
			tesselator.draw();
		} else {
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING);

			for (ItemIdentifierStack itemIdentifierStack : _allItems) {
				ItemIdentifier item = itemIdentifierStack.getItem();
				if (!itemSearched(item))
					continue;
				ppi++;

				if (ppi <= 70 * page)
					continue;
				if (ppi > 70 * (page + 1))
					break;

				ItemStack itemstack = itemIdentifierStack.unsafeMakeNormalStack();
				// -2 on both, because field starts there (see black rect below)
				int realX = guiLeft + x - 2;
				int realY = guiTop + y - 2;

				if (mouseX >= realX && mouseX < realX + panelxSize && mouseY >= realY && mouseY < realY + panelySize) {
					drawRect(x - 2, y - 2, x + panelxSize - 2, y + panelySize - 2, Colors.Black);
					drawRect(x - 1, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.DarkGrey);

					tooltip = new Object[] { mouseX, mouseY, itemstack };
				}

				if (lastClickedx >= realX && lastClickedx < realX + panelxSize && lastClickedy >= realY && lastClickedy < realY + panelySize) {
					selectedItem = itemIdentifierStack;
					drawRect(x - 2, y - 2, x + panelxSize - 2, y + panelySize - 2, Colors.Black);
					drawRect(x - 1, y - 1, x + panelxSize - 3, y + panelySize - 3, Colors.LightGrey);
					drawRect(x, y, x + panelxSize - 4, y + panelySize - 4, Colors.DarkGrey);
					specialItemRendering(itemIdentifierStack.getItem(), x, y);
				}

				String s;
				if (itemstack.stackSize == 1) {
					s = "";
				} else if (itemstack.stackSize < 1000) {
					s = itemstack.stackSize + "";
				} else if (itemstack.stackSize < 100000) {
					s = itemstack.stackSize / 1000 + "K";
				} else if (itemstack.stackSize < 1000000) {
					s = "0M" + itemstack.stackSize / 100000;
				} else {
					s = itemstack.stackSize / 1000000 + "M";
				}

				FontRenderer font = itemstack.getItem().getFontRenderer(itemstack);
				if (font == null)
					font = fontRenderer;

				itemRenderer.zLevel = 100.0F;
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_LIGHTING);
				itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.renderEngine, itemstack, x, y);
				// With empty string, because damage value indicator struggles with the depth
				itemRenderer.renderItemOverlayIntoGUI(font, this.mc.renderEngine, itemstack, x, y, "");
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				itemRenderer.zLevel = 0.0F;

				// Draw number
				font.drawStringWithShadow(s, x + 19 - 2 - font.getStringWidth(s), y + 6 + 3, 16777215);

				x += panelxSize;
				if (x > 200) {
					x = 12;
					y += panelySize;
				}
			}

			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		GL11.glPopMatrix();

		this.guiLeft -= this.getLeftAddition();
	}
	
	public abstract void specialItemRendering(ItemIdentifier item, int x, int y);
	
	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		if(super.hasSubGui()) return;
		BasicGuiHelper.displayItemToolTip(tooltip, this, this.zLevel, guiLeft, guiTop);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if(searchinput1 == "" && searchinput2 == "") return true;
		if(isSearched(item.getFriendlyName().toLowerCase(),(searchinput1 + searchinput2).toLowerCase())) return true;
		if(isSearched(String.valueOf(item.itemID),(searchinput1 + searchinput2))) return true;
		//Enchantment? Enchantment!
		Map<Integer,Integer> enchantIdLvlMap = EnchantmentHelper.getEnchantments(item.unsafeMakeNormalStack(1));
		for(Entry<Integer,Integer> e:enchantIdLvlMap.entrySet()) {
			if (e.getKey().intValue() < Enchantment.enchantmentsList.length && Enchantment.enchantmentsList[e.getKey()] != null) {
				String enchantname = Enchantment.enchantmentsList[e.getKey()].getTranslatedName(e.getValue());
				if(enchantname != null) {
					if(isSearched(enchantname.toLowerCase(),(searchinput1 + searchinput2).toLowerCase())) return true;
				}
			}
		}
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
		int wheel = Mouse.getEventDWheel() / 120;
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

	public void handleRequestAnswer(Collection<ItemIdentifierStack> items, boolean error, ISubGuiControler control, EntityPlayer player) {
		while(control.hasSubGui()) {
			control = control.getSubGui();
		}
		if (error) {
			control.setSubGui(new GuiRequestPopup(_entityPlayer, "You are missing:", items));
		} else {
			control.setSubGui(new GuiRequestPopup(_entityPlayer, "Request successful!", items));
		}
	}

	public void handleSimulateAnswer(Collection<ItemIdentifierStack> used, Collection<ItemIdentifierStack> missing, ISubGuiControler control, EntityPlayer player) {
		while(control.hasSubGui()) {
			control = control.getSubGui();
		}
		control.setSubGui(new GuiRequestPopup(_entityPlayer, "Components: ", used, "Missing: ", missing));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(editsearch) {
			editsearchb = false;
		}
		clickWasButton = true;
		
		if (guibutton.id == 0 && selectedItem != null){
//TODO 		MainProxy.sendPacketToServer(new PacketRequestSubmit(NetworkConstants.REQUEST_SUBMIT,xCoord,yCoord,zCoord,dimension,selectedItem.getItem(),requestCount).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitPacket.class).setDimension(dimension).setStack(selectedItem.getItem().makeStack(requestCount)).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
			refreshItems();
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
//TODO 		MainProxy.sendPacketToServer(new PacketRequestSubmit(NetworkConstants.REQUEST_COMPONENTS, xCoord,yCoord,zCoord,dimension,selectedItem.getItem(), requestCount).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestComponentPacket.class).setDimension(dimension).setStack(selectedItem.getItem().makeStack(requestCount)).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
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
		// Track everything except Escape when in search bar
		if(editsearch && i != 1) {
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