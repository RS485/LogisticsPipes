package logisticspipes.gui.popup;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.lwjgl.input.Keyboard;

import logisticspipes.interfaces.IDiskProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.DiscContent;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

public class GuiAddMacro extends SubGuiScreen implements IItemSearch {

	private final IDiskProvider diskProvider;
	private int mousePosX = 0;
	private int mousePosY = 0;
	private int mousebutton = 0;
	private int pageAll = 0;
	private int maxPageAll = 0;
	private int pageMacro = 0;
	private int maxPageMacro = 0;
	private int wheelup = 0;
	private int wheeldown = 0;
	private boolean editsearch = false;
	private boolean editname = false;
	private LinkedList<ItemIdentifierStack> macroItems = new LinkedList<>();
	private String name1;
	private String name2 = "";
	private String Search1 = "";
	private String Search2 = "";
	private boolean displaycursor = false;
	private long oldSystemTime = 0;

	private Object[] tooltip;

	private static final int NAME_WIDTH = 122;
	private static final int SEARCH_WIDTH = 138;

	public GuiAddMacro(IDiskProvider diskProvider, String macroName) {
		super(200, 200, 0, 0);
		this.diskProvider = diskProvider;
		name1 = macroName;
		loadMacroItems();
	}

	private void loadMacroItems() {
		if ((name1 + name2).equals("")) {
			return;
		}
		NBTTagList inventar = null;

		NBTTagList list = diskProvider.getDisk().getTagCompound().getTagList("macroList", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			String name = tag.getString("name");
			if (name.equals(name1 + name2)) {
				inventar = tag.getTagList("inventar", 10);
				break;
			}
		}
		if (inventar == null) {
			return;
		}
		for (int i = 0; i < inventar.tagCount(); i++) {
			NBTTagCompound itemNBT = inventar.getCompoundTagAt(i);
			int itemID = itemNBT.getInteger("id");
			int itemData = itemNBT.getInteger("data");
			NBTTagCompound tag = null;
			if (itemNBT.hasKey("nbt")) {
				tag = itemNBT.getCompoundTag("nbt");
			}
			ItemIdentifier item = ItemIdentifier.get(Item.getItemById(itemID), itemData, tag);
			int amount = itemNBT.getInteger("amount");
			ItemIdentifierStack stack = new ItemIdentifierStack(item, amount);
			macroItems.add(stack);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, right - 15, guiTop + 5, 10, 10, ">")); // Next pageAll
		buttonList.add(new SmallGuiButton(1, right - 90, guiTop + 5, 10, 10, "<")); // Prev pageAll
		buttonList.add(new SmallGuiButton(2, right - 15, guiTop + 135, 10, 10, ">")); // Next pageAll
		buttonList.add(new SmallGuiButton(3, right - 90, guiTop + 135, 10, 10, "<")); // Prev pageAll
		buttonList.add(new GuiButton(4, right - 39, bottom - 27, 35, 20, "Save")); // Prev pageAll
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		mousePosX = i;
		mousePosY = j;
		mousebutton = k;
		int x = i - guiLeft;
		int y = j - guiTop;
		if (50 < x && x < 188 && 118 < y && y < 133) {
			editsearch = true;
			editname = false;
		} else if (37 < x && x < 159 && 176 < y && y < 190) {
			editsearch = false;
			editname = true;
		} else {
			editsearch = false;
			editname = false;
		}
		super.mouseClicked(i, j, k);
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0) {
			super.handleMouseInputSub();
		}
		if (wheel < 0) {
			wheeldown = wheel * -1;
		} else {
			wheelup = wheel;
		}
	}

	@Override
	protected void renderToolTips(int mouseX, int mouseY, float par3) {
		if (!hasSubGui()) {
			GuiGraphics.displayItemToolTip(tooltip, zLevel, guiLeft, guiTop, false);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int panelxSize = 20;
		int panelySize = 20;

		int ppi = 0;
		int column = 0;
		int row = 0;

		int wheel = org.lwjgl.input.Mouse.getDWheel();
		if (wheel != 0) {
			if (wheel < 0) {
				mousebutton = 0;
			} else {
				mousebutton = 1;
			}
			mousePosX = mouseX;
			mousePosY = mouseY;
		}

		tooltip = null;

		for (ItemIdentifierStack itemidStack : diskProvider.getItemDisplay()._allItems) {
			ItemIdentifier item = itemidStack.getItem();
			if (!itemSearched(item)) {
				continue;
			}
			ppi++;

			if (ppi <= 45 * pageAll) {
				continue;
			}
			if (ppi > 45 * (pageAll + 1)) {
				continue;
			}
			ItemStack st = itemidStack.unsafeMakeNormalStack();
			int x = guiLeft + 10 + panelxSize * column;
			int y = guiTop + 18 + panelySize * row;

			if (!super.hasSubGui()) {
				if (mouseX >= x && mouseX < x + panelxSize && mouseY >= y && mouseY < y + panelySize) {
					Gui.drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, Color.getValue(Color.BLACK));
					Gui.drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, Color.getValue(Color.DARKER_GREY));
					tooltip = new Object[] { mouseX + guiLeft, mouseY + guiTop, st, false };
				}

				if (mousePosX != 0 && mousePosY != 0) {
					if ((mousePosX >= x && mousePosX < x + panelxSize && mousePosY >= y && mousePosY < y + panelySize) || (mouseX >= x && mouseX < x + panelxSize && mouseY >= y && mouseY < y + panelySize && (wheeldown != 0 || wheelup != 0))) {
						boolean handled = false;
						for (ItemIdentifierStack stack : macroItems) {
							if (stack.getItem().equals(item)) {
								if (mousebutton == 0 || wheelup != 0) {
									stack.setStackSize(stack.getStackSize() + (1 + (wheelup != 0 ? wheelup - 1 : 0)));
								} else if (mousebutton == 1 || wheeldown != 0) {
									stack.setStackSize(stack.getStackSize() - (1 + (wheeldown != 0 ? wheeldown - 1 : 0)));
									if (stack.getStackSize() <= 0) {
										macroItems.remove(stack);
									}
								}
								handled = true;
								break;
							}
						}
						if (!handled) {
							int i = 0;
							for (ItemIdentifierStack stack : macroItems) {
								if (item.equals(stack.getItem()) && item.itemDamage < stack.getItem().itemDamage) {
									if (mousebutton == 0 || wheelup != 0) {
										macroItems.add(i, item.makeStack(1 + (wheelup != 0 ? wheelup - 1 : 0)));
									} else if (mousebutton == 2) {
										macroItems.add(i, item.makeStack(64));
									}
									handled = true;
									break;
								}
								if (Item.getIdFromItem(item.item) < Item.getIdFromItem(stack.getItem().item)) {
									if (mousebutton == 0 || wheelup != 0) {
										macroItems.add(i, item.makeStack(1 + (wheelup != 0 ? wheelup - 1 : 0)));
									} else if (mousebutton == 2) {
										macroItems.add(i, item.makeStack(64));
									}
									handled = true;
									break;
								}
								i++;
							}
							if (!handled) {
								if (mousebutton == 0 || wheelup != 0) {
									macroItems.addLast(item.makeStack(1 + (wheelup != 0 ? wheelup - 1 : 0)));
								} else if (mousebutton == 2) {
									macroItems.addLast(item.makeStack(64));
								}
							}
						}
						mousePosX = 0;
						mousePosY = 0;
					}
				}
			}
			column++;
			if (column == 9) {
				row++;
				column = 0;
			}
		}

		ItemStackRenderer.renderItemIdentifierStackListIntoGui(diskProvider.getItemDisplay()._allItems, this, pageAll, guiLeft + 9, guiTop + 17, 9, 45, panelxSize, panelySize, 100.0F, DisplayAmount.NEVER);

		ppi = 0;
		column = 0;
		row = 0;

		for (ItemIdentifierStack itemStack : macroItems) {
			ItemIdentifier item = itemStack.getItem();
			if (!itemSearched(item)) {
				continue;
			}
			ppi++;

			if (ppi <= 9 * pageMacro) {
				continue;
			}
			if (ppi > 9 * (pageMacro + 1)) {
				continue;
			}
			ItemStack st = itemStack.unsafeMakeNormalStack();
			int x = guiLeft + 10 + panelxSize * column;
			int y = guiTop + 150 + panelySize * row;

			if (!super.hasSubGui()) {
				if (mouseX >= x && mouseX < x + panelxSize && mouseY >= y && mouseY < y + panelySize) {
					tooltip = new Object[] { mouseX + guiLeft, mouseY + guiTop, st };
				}
			}
			column++;
			if (column == 9) {
				row++;
				column = 0;
			}
		}
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(macroItems, this, pageMacro, guiLeft + 10, guiTop + 150, 9, 9, panelxSize, panelySize, 100.0F, DisplayAmount.ALWAYS);
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, false);
		mc.fontRenderer.drawString("Add Macro", guiLeft + mc.fontRenderer.getStringWidth("Add Macro") / 2, guiTop + 6, 0x404040);

		maxPageAll = (int) Math.floor((getSearchedItemNumber(diskProvider.getItemDisplay()._allItems) - 1) / 45F);
		if (maxPageAll == -1) {
			maxPageAll = 0;
		}
		if (pageAll > maxPageAll) {
			pageAll = maxPageAll;
		}

		String pageString1 = "Page " + (pageAll + 1) + " / " + (maxPageAll + 1);
		mc.fontRenderer.drawString(pageString1, right - 47 - mc.fontRenderer.getStringWidth(pageString1) / 2, guiTop + 6, 0x404040);

		mc.fontRenderer.drawString("Macro Items", guiLeft + mc.fontRenderer.getStringWidth("Add Macro") / 2, guiTop + 136, 0x404040);

		maxPageMacro = (int) Math.floor((getSearchedItemNumber(macroItems) - 1) / 9F);
		if (maxPageMacro == -1) {
			maxPageMacro = 0;
		}
		if (pageMacro > maxPageMacro) {
			pageMacro = maxPageMacro;
		}

		String pageString2 = "Page " + (pageMacro + 1) + " / " + (maxPageMacro + 1);
		mc.fontRenderer.drawString(pageString2, right - 47 - mc.fontRenderer.getStringWidth(pageString2) / 2, guiTop + 136, 0x404040);

		mc.fontRenderer.drawString("Search:", guiLeft + 8, guiTop + 122, 0x404040);

		if (editsearch) {
			Gui.drawRect(guiLeft + 50, bottom - 66, right - 10, bottom - 83, Color.getValue(Color.BLACK));
			Gui.drawRect(guiLeft + 51, bottom - 67, right - 11, bottom - 82, Color.getValue(Color.WHITE));
		} else {
			Gui.drawRect(guiLeft + 51, bottom - 67, right - 11, bottom - 82, Color.getValue(Color.BLACK));
		}
		Gui.drawRect(guiLeft + 52, bottom - 68, right - 12, bottom - 81, Color.getValue(Color.DARKER_GREY));

		mc.fontRenderer.drawString(Search1 + Search2, guiLeft + 55, guiTop + 122, 0xFFFFFF);

		if (editsearch) {
			int linex = guiLeft + 55 + mc.fontRenderer.getStringWidth(Search1);
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				Gui.drawRect(linex, guiTop + 120, linex + 1, guiTop + 131, Color.getValue(Color.WHITE));
			}
		}

		mc.fontRenderer.drawString("Name:", guiLeft + 8, bottom - 20, 0x404040);

		if (editname) {
			Gui.drawRect(guiLeft + 36, bottom - 8, right - 40, bottom - 25, Color.getValue(Color.BLACK));
			Gui.drawRect(guiLeft + 37, bottom - 9, right - 41, bottom - 24, Color.getValue(Color.WHITE));
		} else {
			Gui.drawRect(guiLeft + 37, bottom - 9, right - 41, bottom - 24, Color.getValue(Color.BLACK));
		}
		Gui.drawRect(guiLeft + 38, bottom - 10, right - 42, bottom - 23, Color.getValue(Color.DARKER_GREY));

		mc.fontRenderer.drawString(name1 + name2, guiLeft + 41, bottom - 20, 0xFFFFFF);

		if (editname) {
			int linex = guiLeft + 41 + mc.fontRenderer.getStringWidth(name1);
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				Gui.drawRect(linex, bottom - 11, linex + 1, bottom - 22, Color.getValue(Color.WHITE));
			}
		}

		Gui.drawRect(guiLeft + 6, guiTop + 16, right - 12, bottom - 84, Color.getValue(Color.GREY));
		Gui.drawRect(guiLeft + 6, bottom - 52, right - 12, bottom - 32, Color.getValue(Color.DARKER_GREY));
	}

	private int getSearchedItemNumber(List<ItemIdentifierStack> list) {
		int count = 0;
		for (ItemIdentifierStack item : list) {
			if (itemSearched(item.getItem())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if (Search1.isEmpty() && Search2.isEmpty()) {
			return true;
		}
		if (isSearched(item.getFriendlyName().toLowerCase(Locale.US), (Search1 + Search2).toLowerCase(Locale.US))) {
			return true;
		}
		return isSearched(String.valueOf(Item.getIdFromItem(item.item)), (Search1 + Search2));
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

	private void nextPageAll() {
		if (pageAll < maxPageAll) {
			pageAll++;
		} else {
			pageAll = 0;
		}
	}

	private void prevPageAll() {
		if (pageAll > 0) {
			pageAll--;
		} else {
			pageAll = maxPageAll;
		}
	}

	private void nextPageMacro() {
		if (pageMacro < maxPageMacro) {
			pageMacro++;
		} else {
			pageMacro = 0;
		}
	}

	private void prevPageMacro() {
		if (pageMacro > 0) {
			pageMacro--;
		} else {
			pageMacro = maxPageMacro;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) {
			nextPageAll();
		} else if (guibutton.id == 1) {
			prevPageAll();
		} else if (guibutton.id == 2) {
			nextPageMacro();
		} else if (guibutton.id == 3) {
			prevPageMacro();
		} else if (guibutton.id == 4) {
			if (!(name1 + name2).equals("") && macroItems.size() != 0) {
				NBTTagList inventar = new NBTTagList();
				for (ItemIdentifierStack stack : macroItems) {
					NBTTagCompound itemNBT = new NBTTagCompound();
					itemNBT.setInteger("id", Item.getIdFromItem(stack.getItem().item));
					itemNBT.setInteger("data", stack.getItem().itemDamage);
					if (stack.getItem().tag != null) {
						itemNBT.setTag("nbt", stack.getItem().tag);
					}
					itemNBT.setInteger("amount", stack.getStackSize());
					inventar.appendTag(itemNBT);
				}

				boolean flag = false;
				NBTTagList list = diskProvider.getDisk().getTagCompound().getTagList("macroList", 10);

				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound tag = list.getCompoundTagAt(i);
					String name = tag.getString("name");
					if (name.equals(name1 + name2)) {
						flag = true;
						tag.setTag("inventar", inventar);
						break;
					}
				}
				if (!flag) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("name", name1 + name2);
					nbt.setTag("inventar", inventar);
					list.appendTag(nbt);
				}
				diskProvider.getDisk().getTagCompound().setTag("macroList", list);
				MainProxy.sendPacketToServer(PacketHandler.getPacket(DiscContent.class).setStack(diskProvider.getDisk()).setPosX(diskProvider.getX()).setPosY(diskProvider.getY()).setPosZ(diskProvider.getZ()));
				exitGui();
			} else if (macroItems.size() != 0) {
				setSubGui(new GuiMessagePopup("Please enter a name"));
			} else {
				setSubGui(new GuiMessagePopup("Select some items"));
			}
		} else {
			super.actionPerformed(guibutton);
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (editname) {
			if (c == 13) {
				editname = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				name1 = name1 + GuiScreen.getClipboardString();
			} else if (c == 8) {
				if (name1.length() > 0) {
					name1 = name1.substring(0, name1.length() - 1);
				}
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (mc.fontRenderer.getStringWidth(name1 + c + name2) <= NAME_WIDTH) {
					name1 += c;
				}
				return;
			} else if (i == 203) { //Left
				if (name1.length() > 0) {
					name2 = name1.substring(name1.length() - 1) + name2;
					name1 = name1.substring(0, name1.length() - 1);
				}
			} else if (i == 205) { //Right
				if (name2.length() > 0) {
					name1 += name2.substring(0, 1);
					name2 = name2.substring(1);
				}
			} else if (i == 1) { //ESC
				editname = false;
			} else if (i == 28) { //Enter
				editname = false;
			} else if (i == 199) { //Pos
				name2 = name1 + name2;
				name1 = "";
			} else if (i == 207) { //Ende
				name1 = name1 + name2;
				name2 = "";
			} else if (i == 211) { //Entf
				if (name2.length() > 0) {
					name2 = name2.substring(1);
				}
			}
		} else if (editsearch) {
			if (c == 13) {
				editsearch = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				Search1 = Search1 + GuiScreen.getClipboardString();
			} else if (c == 8) {
				if (Search1.length() > 0) {
					Search1 = Search1.substring(0, Search1.length() - 1);
				}
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (mc.fontRenderer.getStringWidth(Search1 + c + Search2) <= SEARCH_WIDTH) {
					Search1 += c;
				}
				return;
			} else if (i == 203) { //Left
				if (Search1.length() > 0) {
					Search2 = Search1.substring(Search1.length() - 1) + Search2;
					Search1 = Search1.substring(0, Search1.length() - 1);
				}
			} else if (i == 205) { //Right
				if (Search2.length() > 0) {
					Search1 += Search2.substring(0, 1);
					Search2 = Search2.substring(1);
				}
			} else if (i == 1) { //ESC
				editsearch = false;
			} else if (i == 28) { //Enter
				editsearch = false;
			} else if (i == 199) { //Pos
				Search2 = Search1 + Search2;
				Search1 = "";
			} else if (i == 207) { //Ende
				Search1 = Search1 + Search2;
				Search2 = "";
			} else if (i == 211) { //Entf
				if (Search2.length() > 0) {
					Search2 = Search2.substring(1);
				}
			}
		} else {
			super.keyTyped(c, i);
		}
	}
}
