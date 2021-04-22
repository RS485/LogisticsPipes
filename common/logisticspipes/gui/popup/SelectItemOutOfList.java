package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

import org.lwjgl.input.Keyboard;

import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.TextUtil;

public class SelectItemOutOfList extends SubGuiScreen implements IItemSearch {

	public interface IHandleItemChoice {

		void handleItemChoice(int slot);
	}

	private final List<ItemIdentifierStack> candidate;
	private final IHandleItemChoice handler;
	private ItemDisplay itemDisplay = null;
	private InputBar search;

	public SelectItemOutOfList(List<ItemIdentifierStack> candidate, IHandleItemChoice handler) {
		super(156, 188, 0, 0);
		this.candidate = candidate;
		this.handler = handler;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 70, guiTop + 5, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 138, guiTop + 5, 10, 10, ">"));
		buttonList.add(new GuiButton(2, guiLeft + 100, bottom - 26, 50, 20, "Select"));

		if (search == null) {
			search = new InputBar(fontRenderer, this.getBaseScreen(), guiLeft + 7, bottom - 23, right - guiLeft - 64, 15, false);
		}
		search.reposition(guiLeft + 7, bottom - 23, right - guiLeft - 64, 15);

		if (itemDisplay == null) {
			itemDisplay = new ItemDisplay(this, fontRenderer, this.getBaseScreen(), null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 48, 0, 0, 0, new int[] { 1, 10, 64, 64 }, true);
			itemDisplay.setItemList(candidate);
		}
		itemDisplay.reposition(guiLeft + 8, guiTop + 18, xSize - 16, ySize - 48, 0, 0);
	}

	@Override
	public void exitGui() {
		super.exitGui();
		Keyboard.enableRepeatEvents(false);
		getBaseScreen().initGui();
	}

	@Override
	protected void renderToolTips(int mouseX, int mouseY, float par3) {
		if (!hasSubGui()) {
			GuiGraphics.displayItemToolTip(itemDisplay.getToolTip(), this, zLevel, 0, 0);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		fontRenderer.drawString(TextUtil.translate("misc.selectType"), guiLeft + 8, guiTop + 6, 0x404040, false); //TODO

		itemDisplay.renderPageNumber(right - 47, guiTop + 6);

		//SearchInput
		search.drawTextBox();

		//itemDisplay.renderSortMode(xCenter, bottom - 52);
		itemDisplay.renderItemArea(zLevel);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;
		if (id == 1) {
			itemDisplay.nextPage();
		} else if (id == 0) {
			itemDisplay.prevPage();
		} else if (id == 2) {
			ItemIdentifierStack stack = itemDisplay.getSelectedItem();
			int index = candidate.indexOf(stack);
			if (index >= 0) {
				handler.handleItemChoice(index);
			}
			exitGui();
		}
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		itemDisplay.handleMouse();
		super.handleMouseInputSub();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (!itemDisplay.keyTyped(par1, par2)) {
			if (par2 == 1 || !search.handleKey(par1, par2)) {
				super.keyTyped(par1, par2);
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		if (itemDisplay.handleClick(mouseX, mouseY, button)) return;
		if (search.handleClick(mouseX, mouseY, button)) return;
		super.mouseClicked(mouseX, mouseY, button);
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
		for (Map.Entry<Enchantment, Integer> e : enchantIdLvlMap.entrySet()) {
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
