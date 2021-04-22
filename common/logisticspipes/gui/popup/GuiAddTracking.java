package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

import org.lwjgl.input.Keyboard;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.blocks.stats.TrackingTask;
import logisticspipes.config.Configs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.AddItemToTrackPacket;
import logisticspipes.network.packets.block.RequestAmountTaskSubGui;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiAddTracking extends SubGuiScreen implements IItemSearch {

	private static final String PREFIX = "gui.networkstatistics.add.";

	ItemDisplay itemDisplay;
	InputBar search;
	private final LogisticsStatisticsTileEntity tile;

	public GuiAddTracking(LogisticsStatisticsTileEntity tile) {
		super(160, 200, 0, 0);
		this.tile = tile;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();

		buttonList.clear();
		buttonList.add(new GuiButton(3, guiLeft + 4, bottom - 25, 50, 20, "Refresh")); // Refresh
		buttonList.add(new GuiButton(0, right - 55, bottom - 25, 50, 20, "Add"));
		buttonList.add(new SmallGuiButton(1, right - 15, guiTop + 5, 10, 10, ">")); // Next page
		buttonList.add(new SmallGuiButton(2, right - 90, guiTop + 5, 10, 10, "<")); // Prev page

		buttonList.add(new SmallGuiButton(20, xCenter - 13, bottom - 21, 26, 10, "Sort")); // Sort

		if (search == null) {
			search = new InputBar(fontRenderer, getBaseScreen(), guiLeft + 30, bottom - 78, right - guiLeft - 58, 15);
		}
		search.reposition(guiLeft + 10, bottom - 58, right - guiLeft - 20, 15);

		if (itemDisplay == null) {
			itemDisplay = new ItemDisplay(this, fontRenderer, getBaseScreen(), null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, 0, 0, 0, new int[] { 1, 10, 64, 64 }, true);
		}
		itemDisplay.reposition(guiLeft + 10, guiTop + 18, xSize - 20, ySize - 80, 0, 0);
	}

	@Override
	public void exitGui() {
		super.exitGui();
		Keyboard.enableRepeatEvents(false);
		getBaseScreen().initGui();
	}

	@Override
	protected void renderToolTips(int mouseX, int mouseY, float par3) {
		if (!super.hasSubGui()) {
			GuiGraphics.displayItemToolTip(itemDisplay.getToolTip(), this, zLevel, 0, 0);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		itemDisplay.renderItemArea(zLevel);
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		//mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "title"), guiLeft + 5, guiTop + 6, 0x404040);
		itemDisplay.renderPageNumber(right - 47, guiTop + 6);

		search.drawTextBox();

		itemDisplay.renderSortMode(xCenter, bottom - 32);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0 && itemDisplay.getSelectedItem() != null) {
			boolean found = false;
			for (TrackingTask task : tile.tasks) {
				if (task.item.equals(itemDisplay.getSelectedItem().getItem())) {
					found = true;
					break;
				}
			}
			if (found) {
				setSubGui(new GuiMessagePopup(TextUtil.translate(PREFIX + "alreadytracked")));
			} else {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(AddItemToTrackPacket.class).setItem(itemDisplay.getSelectedItem().getItem()).setTilePos(tile));
				TrackingTask task = new TrackingTask();
				task.item = itemDisplay.getSelectedItem().getItem();
				tile.tasks.add(task);
				exitGui();
			}
		} else if (guibutton.id == 1) {
			itemDisplay.nextPage();
		} else if (guibutton.id == 2) {
			itemDisplay.prevPage();
		} else if (guibutton.id == 3) {
			refreshItems();
		} else if (guibutton.id == 8) {
			GuiCheckBox button = (GuiCheckBox) guibutton;
			Configs.DISPLAY_POPUP = button.change();
			Configs.savePopupState();
		} else if (guibutton.id == 20) {
			itemDisplay.cycle();
		}

		super.actionPerformed(guibutton);
	}

	private void refreshItems() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestAmountTaskSubGui.class).setTilePos(tile));
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		itemDisplay.handleClick(i, j, k);
		search.handleClick(i, j, k);
		super.mouseClicked(i, j, k);
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (i == 201) { //PgUp
			itemDisplay.prevPage();
		} else if (i == 209) { //PgDn
			itemDisplay.nextPage();
		} else {
			// Track everything except Escape when in search bar
			if (i == 1 || !search.handleKey(c, i)) {
				super.keyTyped(c, i);
			}
		}
	}

	public void handlePacket(List<ItemIdentifierStack> identList) {
		if (itemDisplay == null) {
			itemDisplay = new ItemDisplay(this, fontRenderer, getBaseScreen(), null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, 0, 0, 0, new int[] { 1, 10, 64, 64 }, true);
		}
		itemDisplay.setItemList(identList);
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
}
