/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.orderer;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.Configs;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.interfaces.ISpecialItemRenderer;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestComponentPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SearchBar;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

public abstract class GuiOrderer extends LogisticsBaseGuiScreen implements IItemSearch, ISpecialItemRenderer {

	public final EntityPlayer _entityPlayer;
	public ItemDisplay itemDisplay;
	private SearchBar search;
	
	protected String _title = "Request items";

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
		itemDisplay.setItemList(allItems);
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
		
		buttonList.add(new SmallGuiButton(20, xCenter - 13, bottom - 41, 26, 10, "Sort")); // Sort
		
		if(search == null) search = new SearchBar(mc.fontRenderer, this, guiLeft + 30, bottom - 78, right - guiLeft - 58, 15);
		search.reposition(guiLeft + 30, bottom - 78, right - guiLeft - 58, 15);
		
		if(itemDisplay == null) itemDisplay = new ItemDisplay(this, mc.fontRenderer, this, this, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, new int[]{1,10,64,64}, true);
		itemDisplay.reposition(guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);

		mc.fontRenderer.drawString(_title, guiLeft + mc.fontRenderer.getStringWidth(_title) / 2, guiTop + 6, 0x404040);
		itemDisplay.renderPageNumber(right - 47, guiTop + 6);
		
		
		if(buttonList.get(9) instanceof GuiCheckBox && ((GuiCheckBox)buttonList.get(9)).getState()) {
			mc.fontRenderer.drawString("Popup", guiLeft + 25 , bottom - 56, 0x404040);
		} else {
			mc.fontRenderer.drawString("Popup", guiLeft + 25 , bottom - 56, 0xA0A0A0);
		}
		
		itemDisplay.renderAmount(xCenter, bottom - 24, getStackAmount());
		//SearchInput
		search.renderSearchBar();
		
		itemDisplay.renderSortMode(xCenter, bottom - 52);
		itemDisplay.renderItemArea(zLevel);
	}
	
	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		if(super.hasSubGui()) return;
		BasicGuiHelper.displayItemToolTip(itemDisplay.getToolTip(), this, this.zLevel, guiLeft, guiTop);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if(search.isEmpty()) return true;
		if(isSearched(item.getFriendlyName().toLowerCase(), search.getContent().toLowerCase())) return true;
		//if(isSearched(String.valueOf(Item.getIdFromItem(item.item)), search.getContent())) return true;
		//Enchantment? Enchantment!
		Map<Integer,Integer> enchantIdLvlMap = EnchantmentHelper.getEnchantments(item.unsafeMakeNormalStack(1));
		for(Entry<Integer,Integer> e:enchantIdLvlMap.entrySet()) {
			if (e.getKey().intValue() < Enchantment.enchantmentsList.length && Enchantment.enchantmentsList[e.getKey()] != null) {
				String enchantname = Enchantment.enchantmentsList[e.getKey()].getTranslatedName(e.getValue());
				if(enchantname != null) {
					if(isSearched(enchantname.toLowerCase(), search.getContent().toLowerCase())) return true;
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
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		itemDisplay.handleClick(i, j, k);
		search.handleClick(i, j, k);
		super.mouseClicked(i, j, k);
	}
	
	@Override
	public void handleMouseInputSub() {
		itemDisplay.handleMouse();
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
		if (guibutton.id == 0 && itemDisplay.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitPacket.class).setDimension(dimension).setStack(itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount())).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
			refreshItems();
		} else if (guibutton.id == 1){
			itemDisplay.nextPage();
		} else if (guibutton.id == 2) {
			itemDisplay.prevPage();
		} else if (guibutton.id == 3) {
			refreshItems();
		} else if (guibutton.id == 10) {
			itemDisplay.sub(3);
		} else if (guibutton.id == 4) {
			itemDisplay.sub(2);
		} else if (guibutton.id == 5) {
			itemDisplay.sub(1);
		} else if (guibutton.id == 6) {
			itemDisplay.add(1);
		} else if (guibutton.id == 7) {
			itemDisplay.add(2);
		} else if (guibutton.id == 11) {
			itemDisplay.add(3);
		} else if (guibutton.id == 8) {
			GuiCheckBox button = (GuiCheckBox)guibutton;
			Configs.DISPLAY_POPUP = button.change();
			Configs.savePopupState();
		} else if (guibutton.id == 13 && itemDisplay.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestComponentPacket.class).setDimension(dimension).setStack(itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount())).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
		} else if (guibutton.id == 20) {
			itemDisplay.cycle();
		}
		
		super.actionPerformed(guibutton);
	}

	protected int getStackAmount() {
		return 64;
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (i == 30 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-a
			itemDisplay.setMaxAmount();
		} else if (i == 32 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-d
			itemDisplay.resetAmount();
		} else if (i == 201) { //PgUp
			itemDisplay.prevPage();
		} else if (i == 209) { //PgDn
			itemDisplay.nextPage();
		} else {
			// Track everything except Escape when in search bar
			if(i == 1 || !search.handleKey(c, i)) {
				super.keyTyped(c, i);
			}
		}
	}

	@Override
	public void resetSubGui() {
		super.resetSubGui();
		refreshItems();
	}
}