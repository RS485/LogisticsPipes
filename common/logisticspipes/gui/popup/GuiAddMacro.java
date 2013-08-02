package logisticspipes.gui.popup;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.DiscContent;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.KraphtBaseGuiScreen.Colors;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiAddMacro extends SubGuiScreen implements IItemSearch {

	private NormalMk2GuiOrderer mainGui;
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
	private LinkedList<ItemIdentifierStack> macroItems = new LinkedList<ItemIdentifierStack>();
	private String name1="";
	private String name2="";
	private String Search1="";
	private String Search2="";
	private boolean displaycursor = false;
	private long oldSystemTime=0;
	
	private Object[] tooltip;
	
	private int nameWidth = 122;
	private int searchWidth = 138;
	
	public GuiAddMacro(NormalMk2GuiOrderer mainGui, String macroName) {
		super(200, 200, 0, 0);
		this.mainGui = mainGui;
		name1 = macroName;
		loadMacroItems();
	}

	private void loadMacroItems() {
		if((name1 + name2).equals("")) {
			return;
		}
		NBTTagList inventar = null;

		NBTTagList list = this.mainGui.getDisk().getTagCompound().getTagList("macroList");
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);
			String name = tag.getString("name");
			if(name.equals(name1 + name2)) {
				inventar = tag.getTagList("inventar");
				break;
			}
		}
		if(inventar == null) {
			return;
		}
		for(int i = 0; i < inventar.tagCount(); i++) {
			NBTTagCompound itemNBT = (NBTTagCompound) inventar.tagAt(i);
			int itemID = itemNBT.getInteger("id");
			int itemData = itemNBT.getInteger("data");
			NBTTagCompound tag = null;
			if(itemNBT.hasKey("nbt")) {
				tag = itemNBT.getCompoundTag("nbt");
			}
			ItemIdentifier item = ItemIdentifier.get(itemID, itemData, tag);
			int amount = itemNBT.getInteger("amount");
			ItemIdentifierStack stack = new ItemIdentifierStack(item, amount);
			macroItems.add(stack);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, right - 15, guiTop + 5, 10 ,10 ,">")); // Next pageAll
		buttonList.add(new SmallGuiButton(1, right - 90, guiTop + 5, 10, 10, "<")); // Prev pageAll
		buttonList.add(new SmallGuiButton(2, right - 15, guiTop + 135, 10 ,10 ,">")); // Next pageAll
		buttonList.add(new SmallGuiButton(3, right - 90, guiTop + 135, 10, 10, "<")); // Prev pageAll
		buttonList.add(new GuiButton(4, right - 39, bottom - 27, 35, 20, "Save")); // Prev pageAll
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		mousePosX = i;
		mousePosY = j;
		mousebutton = k;
		int x = i - guiLeft;
		int y = j - guiTop;
		if (50 < x && x < 188
		&& 118 < y && y < 133) {
			editsearch = true;
			editname = false;
		} else if(37 < x && x < 159
			  && 176 < y && y < 190) {
			editsearch = false;
			editname = true;
		} else {
			editsearch = false;
			editname = false;
		}
		super.mouseClicked(i, j, k);
	}
	

	@Override
	public void handleMouseInputSub() {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if(wheel == 0) super.handleMouseInputSub();
		if(wheel < 0) {
			wheeldown = wheel * -1;
		} else {
			wheelup = wheel;
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3){
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, false);
		
		fontRenderer.drawString("Add Macro", guiLeft + fontRenderer.getStringWidth("Add Macro") / 2, guiTop + 6, 0x404040);
		
		maxPageAll = (int) Math.floor((getSearchedItemNumber(mainGui._allItems) - 1)  / 45F);
		if(maxPageAll == -1) maxPageAll = 0;
		if (pageAll > maxPageAll){
			pageAll = maxPageAll;
		}
		
		String pageString1 = "Page " + (pageAll + 1) + " / " + (maxPageAll + 1);
		fontRenderer.drawString(pageString1, right - 47 - fontRenderer.getStringWidth(pageString1) / 2 , guiTop + 6 , 0x404040);
		
		
		fontRenderer.drawString("Macro Items", guiLeft + fontRenderer.getStringWidth("Add Macro") / 2, guiTop + 136, 0x404040);
		
		maxPageMacro = (int) Math.floor((getSearchedItemNumber(macroItems) - 1)  / 9F);
		if(maxPageMacro == -1) maxPageMacro = 0;
		if (pageMacro > maxPageMacro){
			pageMacro = maxPageMacro;
		}
		
		String pageString2 = "Page " + (pageMacro + 1) + " / " + (maxPageMacro + 1);
		fontRenderer.drawString(pageString2, right - 47 - fontRenderer.getStringWidth(pageString2) / 2 , guiTop + 136 , 0x404040);
		
		
		fontRenderer.drawString("Search:", guiLeft + 8, guiTop + 122, 0x404040);
		
		if(editsearch) {
			drawRect(guiLeft + 50, bottom - 66, right - 10, bottom - 83, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
			drawRect(guiLeft + 51, bottom - 67, right - 11, bottom - 82, BasicGuiHelper.ConvertEnumToColor(Colors.White));
		} else {
			drawRect(guiLeft + 51, bottom - 67, right - 11, bottom - 82, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
		}
		drawRect(guiLeft + 52, bottom - 68, right - 12, bottom - 81, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
		
		fontRenderer.drawString(Search1 + Search2, guiLeft + 55, guiTop + 122, 0xFFFFFF);
		
		if(editsearch) {
			int linex = guiLeft + 55 + fontRenderer.getStringWidth(Search1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, guiTop + 120, linex + 1, guiTop + 131, BasicGuiHelper.ConvertEnumToColor(Colors.White));
			}
		}
		
		
		
		fontRenderer.drawString("Name:", guiLeft + 8, bottom - 20, 0x404040);
		
		if(editname) {
			drawRect(guiLeft + 36, bottom - 8, right - 40, bottom - 25, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
			drawRect(guiLeft + 37, bottom - 9, right - 41, bottom - 24, BasicGuiHelper.ConvertEnumToColor(Colors.White));
		} else {
			drawRect(guiLeft + 37, bottom - 9, right - 41, bottom - 24, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
		}
		drawRect(guiLeft + 38, bottom - 10, right - 42, bottom - 23, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
		
		fontRenderer.drawString(name1 + name2, guiLeft + 41, bottom - 20, 0xFFFFFF);

		if(editname) {
			int linex = guiLeft + 41 + fontRenderer.getStringWidth(name1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, bottom - 11, linex + 1, bottom - 22, BasicGuiHelper.ConvertEnumToColor(Colors.White));
			}
		}
		
		
		
		int panelxSize = 20;
		int panelySize = 20;

		int ppi = 0;
		int column = 0;
		int row = 0;
		
		int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
		
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if(wheel != 0) {
			if(wheel < 0) {
				mousebutton = 0;
			} else {
				mousebutton = 1;
			}
			mousePosX = mouseX;
			mousePosY = mouseY;
		}
		
		tooltip = null;
		
		drawRect(guiLeft + 6, guiTop + 16, right - 12, bottom - 84, BasicGuiHelper.ConvertEnumToColor(Colors.MiddleGrey));
		drawRect(guiLeft + 6, bottom - 52, right - 12, bottom - 32, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
		
		for(ItemIdentifierStack itemStack : mainGui._allItems) {
			ItemIdentifier item = itemStack.getItem();
			if(!itemSearched(item)) continue;
			ppi++;
			
			if (ppi <= 45 * pageAll) continue;
			if (ppi > 45 * (pageAll+1)) continue;
			ItemStack st = itemStack.unsafeMakeNormalStack();
			int x = guiLeft + 10 + panelxSize * column;
			int y = guiTop + 18 + panelySize * row;

			GL11.glDisable(2896 /*GL_LIGHTING*/);
			
            if(!super.hasSubGui()) {
				if (mouseX >= x && mouseX < x + panelxSize && mouseY >= y && mouseY < y + panelySize) {
					drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
					drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
					
					tooltip = new Object[]{mouseX + guiLeft,mouseY + guiTop,st, false};
				}
				
				
				if(mousePosX != 0 && mousePosY != 0) {
					if ((mousePosX >= x && mousePosX < x + panelxSize && mousePosY >= y && mousePosY < y + panelySize) || (mouseX >= x && mouseX < x + panelxSize && mouseY >= y && mouseY < y + panelySize && (wheeldown != 0 || wheelup != 0))) {
						boolean handled = false;
						for(ItemIdentifierStack stack:macroItems) {
							if(stack.getItem().equals(item)) {
								if(mousebutton == 0 || wheelup != 0) {
									stack.stackSize += 1 + (wheelup != 0 ? wheelup - 1: 0);
								} else if(mousebutton == 1 || wheeldown != 0) {
									stack.stackSize -= 1 + (wheeldown != 0 ? wheeldown - 1: 0);
									if(stack.stackSize <= 0) {
										macroItems.remove(stack);
									}
								}
								handled = true;
								break;
							}
						}
						if(!handled) {
							int i = 0;
							for(ItemIdentifierStack stack:macroItems) {
								if(item.itemID == stack.getItem().itemID && item.itemDamage < stack.getItem().itemDamage) {
									if(mousebutton == 0 || wheelup != 0) {
										macroItems.add(i, item.makeStack(1 + (wheelup != 0 ? wheelup - 1: 0)));
									} else if(mousebutton == 2) {
										macroItems.add(i, item.makeStack(64));
									}
									handled = true;
									break;
								}
								if(item.itemID < stack.getItem().itemID) {
									if(mousebutton == 0 || wheelup != 0) {
										macroItems.add(i, item.makeStack(1 + (wheelup != 0 ? wheelup - 1: 0)));
									} else if(mousebutton == 2) {
										macroItems.add(i, item.makeStack(64));
									}
									handled = true;
									break;
								}
								i++;
							}
							if(!handled) {
								if(mousebutton == 0 || wheelup != 0) {
									macroItems.addLast(item.makeStack(1 + (wheelup != 0 ? wheelup - 1: 0)));
								} else if(mousebutton == 2) {
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
			if (column == 9){
				row++;
				column = 0;
			}
		}

		BasicGuiHelper.renderItemIdentifierStackListIntoGui(mainGui._allItems, this, pageAll, guiLeft + 10, guiTop + 18, 9, 45, panelxSize, panelySize, mc, false, false);

		ppi = 0;
		column = 0;
		row = 0;
		
		for(ItemIdentifierStack itemStack : macroItems) {
			ItemIdentifier item = itemStack.getItem();
			if(!itemSearched(item)) continue;
			ppi++;
			
			if (ppi <= 9 * pageMacro) continue;
			if (ppi > 9 * (pageMacro+1)) continue;
			ItemStack st = itemStack.unsafeMakeNormalStack();
			int x = guiLeft + 10 + panelxSize * column;
			int y = guiTop + 150 + panelySize * row;

			GL11.glDisable(2896 /*GL_LIGHTING*/);
			
            if(!super.hasSubGui()) {
				if (mouseX >= x && mouseX < x + panelxSize && mouseY >= y && mouseY < y + panelySize) {
					//drawRect(x - 3, y - 1, x + panelxSize - 3, y + panelySize - 3, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
					//drawRect(x - 2, y - 0, x + panelxSize - 4, y + panelySize - 4, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
					
					tooltip = new Object[]{mouseX + guiLeft,mouseY + guiTop,st};
				}	
            }
			column++;
			if (column == 9){
				row++;
				column = 0;
			}
		}
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(macroItems, this, pageMacro, guiLeft + 10, guiTop + 150, 9, 9, panelxSize, panelySize, mc, true, true);

		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		super.drawScreen(par1, par2, par3);
		
		if(!this.hasSubGui()) {
			BasicGuiHelper.displayItemToolTip(tooltip, this, 300, guiLeft, guiTop, true, false);
		}
	}


	private int getSearchedItemNumber(List<ItemIdentifierStack> list) {
		int count = 0;
		for(ItemIdentifierStack item : list) {
			if(itemSearched(item.getItem())) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if(Search1 == "" && Search2 == "") return true;
		if(isSearched(item.getFriendlyName().toLowerCase(),(Search1 + Search2).toLowerCase())) return true;
		if(isSearched(String.valueOf(item.itemID),(Search1 + Search2))) return true;
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
	
	private void nextPageAll(){
		if (pageAll < maxPageAll){
			pageAll++;
		} else {
			pageAll = 0;
		}
	}
	
	private void prevPageAll(){
		if (pageAll > 0){
			pageAll--;
		} else {
			pageAll = maxPageAll;
		}
	}

	private void nextPageMacro(){
		if (pageMacro < maxPageMacro){
			pageMacro++;
		} else {
			pageMacro = 0;
		}
	}
	
	private void prevPageMacro(){
		if (pageMacro > 0){
			pageMacro--;
		} else {
			pageMacro = maxPageMacro;
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0) {
			nextPageAll();
		} else if (guibutton.id == 1) {
			prevPageAll();
		} else if (guibutton.id == 2) {
			nextPageMacro();
		} else if (guibutton.id == 3) {
			prevPageMacro();
		} else if (guibutton.id == 4) {
			if(!(name1 + name2).equals("") && macroItems.size() != 0) {
				NBTTagList inventar = new NBTTagList();
				for(ItemIdentifierStack stack:macroItems) {
					NBTTagCompound itemNBT = new NBTTagCompound();
					itemNBT.setInteger("id", stack.getItem().itemID);
					itemNBT.setInteger("data", stack.getItem().itemDamage);
					if(stack.getItem().tag != null) {
						itemNBT.setCompoundTag("nbt", stack.getItem().tag);
					}
					itemNBT.setInteger("amount", stack.stackSize);
					inventar.appendTag(itemNBT);
				}

				boolean flag = false;
				NBTTagList list = this.mainGui.getDisk().getTagCompound().getTagList("macroList");

				for(int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);
					String name = tag.getString("name");
					if(name.equals(name1 + name2)) {
						flag = true;
						tag.setTag("inventar", inventar);
						break;
					}
				}
				if(!flag) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("name", name1 + name2);
					nbt.setTag("inventar", inventar);
					list.appendTag(nbt);
				}
				this.mainGui.getDisk().getTagCompound().setTag("macroList", list);
//TODO 			MainProxy.sendPacketToServer(new PacketItem(NetworkConstants.DISK_CONTENT, mainGui.pipe.getX(), mainGui.pipe.getY(), mainGui.pipe.getZ(), mainGui.pipe.getDisk()).getPacket());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(DiscContent.class).setStack(mainGui.pipe.getDisk()).setPosX(mainGui.pipe.getX()).setPosY(mainGui.pipe.getY()).setPosZ(mainGui.pipe.getZ()));
				this.exitGui();
			} else if(macroItems.size() != 0) {
				this.setSubGui(new GuiMessagePopup("Please enter a name"));
			} else {
				this.setSubGui(new GuiMessagePopup("Select some items"));
			}
		} else {
			super.actionPerformed(guibutton);
		}
	}
	

	@Override
	protected void keyTyped(char c, int i) {
		if(editname) {
			if (c == 13) {
				editname = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				name1 = name1 + getClipboardString();
			} else if (c == 8) {
				if (name1.length() > 0)
					name1 = name1.substring(0, name1.length() - 1);
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (fontRenderer.getStringWidth(name1 + c + name2) <= nameWidth) {
					name1 += c;
				}
				return;
			} else if(i == 203) { //Left
				if(name1.length() > 0) {
					name2 = name1.substring(name1.length() - 1) + name2;
					name1 = name1.substring(0, name1.length() - 1);
				}
			} else if(i == 205) { //Right
				if(name2.length() > 0) {
					name1 += name2.substring(0,1);
					name2 = name2.substring(1);
				}
			} else if(i == 1) { //ESC
				editname = false;
			} else if(i == 28) { //Enter
				editname = false;
			} else if(i == 199) { //Pos
				name2 = name1 + name2;
				name1 = "";
			} else if(i == 207) { //Ende
				name1 = name1 + name2;
				name2 = "";
			} else if(i == 211) { //Entf
				if (name2.length() > 0)
					name2 = name2.substring(1);
			}
		} else if(editsearch) {
			if (c == 13) {
				editsearch = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				Search1 = Search1 + getClipboardString();
			} else if (c == 8) {
				if (Search1.length() > 0)
					Search1 = Search1.substring(0, Search1.length() - 1);
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (fontRenderer.getStringWidth(Search1 + c + Search2) <= searchWidth) {
					Search1 += c;
				}
				return;
			} else if(i == 203) { //Left
				if(Search1.length() > 0) {
					Search2 = Search1.substring(Search1.length() - 1) + Search2;
					Search1 = Search1.substring(0, Search1.length() - 1);
				}
			} else if(i == 205) { //Right
				if(Search2.length() > 0) {
					Search1 += Search2.substring(0,1);
					Search2 = Search2.substring(1);
				}
			} else if(i == 1) { //ESC
				editsearch = false;
			} else if(i == 28) { //Enter
				editsearch = false;
			} else if(i == 199) { //Pos
				Search2 = Search1 + Search2;
				Search1 = "";
			} else if(i == 207) { //Ende
				Search1 = Search1 + Search2;
				Search2 = "";
			} else if(i == 211) { //Entf
				if (Search2.length() > 0)
					Search2 = Search2.substring(1);
			}
		} else {
			super.keyTyped(c, i);
		}
	}
}
