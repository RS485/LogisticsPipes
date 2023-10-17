/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.orderer;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;

import logisticspipes.config.Configs;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.interfaces.ISpecialItemRenderer;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestComponentPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.resources.IResource;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public abstract class GuiOrderer extends LogisticsBaseGuiScreen implements IItemSearch, ISpecialItemRenderer {

	public final EntityPlayer _entityPlayer;
	public ItemDisplay itemDisplay;
	private InputBar search;

	protected String _title = "Request items";

	public final int xCoord;
	public final int yCoord;
	public final int zCoord;
	public int dimension;

	public static int dimensioncache;
	public static long cachetime;

	public GuiOrderer(int x, int y, int z, int dim, EntityPlayer entityPlayer) {
		super(220, 240, 0, 0);
		xCoord = x;
		yCoord = y;
		zCoord = z;
		if (GuiOrderer.cachetime + 100 < System.currentTimeMillis()) {
			dimension = dim;
		} else {
			dimension = GuiOrderer.dimensioncache;
		}
		_entityPlayer = entityPlayer;
		inventorySlots = new DummyContainer(entityPlayer.inventory, null);
	}

	public abstract void refreshItems();

	public void handlePacket(Collection<ItemIdentifierStack> allItems) {
		itemDisplay.setItemList(allItems.stream().filter(Objects::nonNull).collect(Collectors.toList()));
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();

		buttonList.clear();
		buttonList.add(new GuiButton(0, right - 55, bottom - 25, 50, 20, "Request")); // Request
		buttonList.add(new SmallGuiButton(1, right - 15, guiTop + 5, 10, 10, ">")); // Next page
		buttonList.add(new SmallGuiButton(2, right - 90, guiTop + 5, 10, 10, "<")); // Prev page
		buttonList.add(new SmallGuiButton(10, xCenter - 51, bottom - 15, 26, 10, "---")); // -64
		buttonList.add(new SmallGuiButton(4, xCenter - 51, bottom - 26, 15, 10, "--")); // -10
		buttonList.add(new SmallGuiButton(5, xCenter - 35, bottom - 26, 10, 10, "-")); // -1
		buttonList.add(new SmallGuiButton(6, xCenter + 26, bottom - 26, 10, 10, "+")); // +1
		buttonList.add(new SmallGuiButton(7, xCenter + 38, bottom - 26, 15, 10, "++")); // +10
		buttonList.add(new SmallGuiButton(11, xCenter + 26, bottom - 15, 26, 10, "+++")); // +64
		buttonList.add(new GuiCheckBox(8, guiLeft + 9, bottom - 60, 14, 14, Configs.DISPLAY_POPUP)); // Popup

		buttonList.add(new SmallGuiButton(20, xCenter - 13, bottom - 41, 26, 10, "Sort")); // Sort

		if (search == null) {
			search = new InputBar(fontRenderer, this, guiLeft + 10, bottom - 78, xSize - 20, 15);
		}
		search.reposition(guiLeft + 10, bottom - 78, xSize - 20, 15);

		if (itemDisplay == null) {
			itemDisplay = new ItemDisplay(this, fontRenderer, this, this, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, xCenter, bottom - 24, 49, new int[] { 1, 10, 64, 64 }, true);
		}
		itemDisplay.reposition(guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, xCenter, bottom - 24);
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);

		mc.fontRenderer.drawString(_title, guiLeft + mc.fontRenderer.getStringWidth(_title) / 2, guiTop + 6, 0x404040);
		itemDisplay.renderPageNumber(right - 47, guiTop + 6);

		if (buttonList.get(9) instanceof GuiCheckBox && ((GuiCheckBox) buttonList.get(9)).getState()) {
			mc.fontRenderer.drawString("Popup", guiLeft + 25, bottom - 56, 0x404040);
		} else {
			mc.fontRenderer.drawString("Popup", guiLeft + 25, bottom - 56, Color.getValue(Color.GREY));
		}

		itemDisplay.renderAmount(getStackAmount());
		//SearchInput
		search.drawTextBox();

		itemDisplay.renderSortMode(xCenter, bottom - 52);
		itemDisplay.renderItemArea(zLevel);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (super.hasSubGui()) {
			return;
		}
		GuiGraphics.displayItemToolTip(itemDisplay.getToolTip(), this, zLevel, guiLeft, guiTop);
	}

	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if (search.isEmpty()) {
			return true;
		}
		if (isSearched(item.getFriendlyName().toLowerCase(Locale.US), search.getText().toLowerCase(Locale.US))) {
			return true;
		}
		//if(isSearched(String.valueOf(Item.getIdFromItem(item.item)), search.getContent())) return true;
		//Enchantment? Enchantment!
		Map<Enchantment, Integer> enchantIdLvlMap = EnchantmentHelper.getEnchantments(item.unsafeMakeNormalStack(1));
		for (Entry<Enchantment, Integer> e : enchantIdLvlMap.entrySet()) {
			String enchantname = e.getKey().getName();
			if (enchantname != null) {
				if (isSearched(enchantname.toLowerCase(Locale.US), search.getText().toLowerCase(Locale.US))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isSearched(String value, String search) {
		boolean flag = true;
		for (String s : search.split(" ")) {
			if (!value.contains(s)) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		itemDisplay.handleClick(i, j, k);
		search.handleClick(i, j, k);
		super.mouseClicked(i, j, k);
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		itemDisplay.handleMouse();
		super.handleMouseInputSub();
	}

	public void handleRequestAnswer(Collection<IResource> items, boolean error, ISubGuiControler control, EntityPlayer player) {
		while (control.hasSubGui()) {
			control = control.getSubGui();
		}
		if (error) {
			control.setSubGui(new GuiRequestPopup(_entityPlayer, "You are missing:", items));
		} else {
			control.setSubGui(new GuiRequestPopup(_entityPlayer, "Request successful!", items));
		}
	}

	public void handleSimulateAnswer(Collection<IResource> used, Collection<IResource> missing, ISubGuiControler control, EntityPlayer player) {
		while (control.hasSubGui()) {
			control = control.getSubGui();
		}
		control.setSubGui(new GuiRequestPopup(_entityPlayer, "Components: ", used, "Missing: ", missing));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0 && itemDisplay.getSelectedItem() != null) {
			final ItemIdentifierStack stack = itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitPacket.class).setStack(stack).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord).setDimension(dimension));
			refreshItems();
		} else if (guibutton.id == 1) {
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
			GuiCheckBox button = (GuiCheckBox) guibutton;
			Configs.DISPLAY_POPUP = button.change();
			Configs.savePopupState();
		} else if (guibutton.id == 13 && itemDisplay.getSelectedItem() != null) {
			final ItemIdentifierStack stack = itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestComponentPacket.class).setStack(stack).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord).setDimension(dimension));
		} else if (guibutton.id == 20) {
			itemDisplay.cycle();
		}

		super.actionPerformed(guibutton);
	}

	protected int getStackAmount() {
		return 64;
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (search.isFocused()) {
			if (!search.isEmpty() && search.handleKey(c, i))
				return;
		} else if (GuiScreen.isAltKeyDown() && ChatAllowedCharacters.isAllowedCharacter(c)) {
			itemDisplay.setFocused(false);
			search.setFocused(true);
			search.setText("");
			search.handleKey(c, i);
			return;
		}
		if (!itemDisplay.keyTyped(c, i)) {
			// Track everything except Escape when in search bar
			if (i == 1 || !search.handleKey(c, i)) {
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
