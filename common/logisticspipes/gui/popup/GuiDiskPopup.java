package logisticspipes.gui.popup;

import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.lwjgl.input.Keyboard;

import logisticspipes.interfaces.IDiskProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.DiscContent;
import logisticspipes.network.packets.orderer.DiskMacroRequestPacket;
import logisticspipes.network.packets.orderer.DiskSetNamePacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.gui.TextListDisplay;

public class GuiDiskPopup extends SubGuiScreen {

	private boolean editname = false;
	private boolean displaycursor = false;
	private long oldSystemTime = 0;
	private String name1;
	private String name2;
	private final IDiskProvider diskProvider;
	private final TextListDisplay textList;

	private static final int SEARCH_WIDTH = 120;

	public GuiDiskPopup(IDiskProvider diskProvider) {
		super(150, 200, 0, 0);
		this.diskProvider = diskProvider;
		name2 = "";
		if (diskProvider.getDisk().hasTagCompound()) {
			name1 = diskProvider.getDisk().getTagCompound().getString("name");
		} else {
			name1 = "Disk";
		}
		textList = new TextListDisplay(this, 6, 46, 6, 30, 12, new TextListDisplay.List() {

			@Override
			public int getSize() {
				NBTTagCompound nbt = diskProvider.getDisk().getTagCompound();
				if (nbt == null) {
					diskProvider.getDisk().setTagCompound(new NBTTagCompound());
					nbt = diskProvider.getDisk().getTagCompound();
				}

				if (!nbt.hasKey("macroList")) {
					NBTTagList list = new NBTTagList();
					nbt.setTag("macroList", list);
				}
				NBTTagList list = nbt.getTagList("macroList", 10);
				return list.tagCount();
			}

			@Override
			public String getTextAt(int index) {
				NBTTagCompound nbt = diskProvider.getDisk().getTagCompound();
				if (nbt == null) {
					diskProvider.getDisk().setTagCompound(new NBTTagCompound());
					nbt = diskProvider.getDisk().getTagCompound();
				}

				if (!nbt.hasKey("macroList")) {
					NBTTagList list = new NBTTagList();
					nbt.setTag("macroList", list);
				}
				NBTTagList list = nbt.getTagList("macroList", 10);
				return list.getCompoundTagAt(index).getString("name");
			}

			@Override
			public int getTextColor(int index) {
				return 0xFFFFFF;
			}
		});
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		int x = i - guiLeft;
		int y = j - guiTop;
		textList.mouseClicked(i, j, k);
		if (k == 0) {
			if (10 < x && x < 138 && 29 < y && y < 44) {
				editname = true;
			} else if (editname) {
				writeDiskName();
			} else {
				super.mouseClicked(i, j, k);
			}
		} else {
			super.mouseClicked(i, j, k);
		}
	}

	private void writeDiskName() {
		editname = false;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskSetNamePacket.class).setString(name1 + name2).setPosX(diskProvider.getX()).setPosY(diskProvider.getY()).setPosZ(diskProvider.getZ()));
		NBTTagCompound nbt = new NBTTagCompound();
		if (diskProvider.getDisk().hasTagCompound()) {
			nbt = diskProvider.getDisk().getTagCompound();
		}
		nbt.setString("name", name1 + name2);
		diskProvider.getDisk().setTagCompound(nbt);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiscContent.class).setStack(diskProvider.getDisk()).setPosX(diskProvider.getX()).setPosY(diskProvider.getY()).setPosZ(diskProvider.getZ()));
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, xCenter + 16, bottom - 27, 50, 10, "Request"));
		buttonList.add(new SmallGuiButton(1, xCenter + 16, bottom - 15, 50, 10, "Exit"));
		buttonList.add(new SmallGuiButton(2, xCenter - 66, bottom - 27, 50, 10, "Add/Edit"));
		buttonList.add(new SmallGuiButton(3, xCenter - 66, bottom - 15, 50, 10, "Delete"));
		buttonList.add(new SmallGuiButton(4, xCenter - 12, bottom - 27, 25, 10, "/\\"));
		buttonList.add(new SmallGuiButton(5, xCenter - 12, bottom - 15, 25, 10, "\\/"));
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		mc.fontRenderer.drawStringWithShadow("Disk", xCenter - (mc.fontRenderer.getStringWidth("Disk") / 2), guiTop + 10, 0xFFFFFF);

		//NameInput
		if (editname) {
			Gui.drawRect(guiLeft + 10, guiTop + 28, right - 10, guiTop + 45, Color.getValue(Color.BLACK));
			Gui.drawRect(guiLeft + 11, guiTop + 29, right - 11, guiTop + 44, Color.getValue(Color.WHITE));
		} else {
			Gui.drawRect(guiLeft + 11, guiTop + 29, right - 11, guiTop + 44, Color.getValue(Color.BLACK));
		}
		Gui.drawRect(guiLeft + 12, guiTop + 30, right - 12, guiTop + 43, Color.getValue(Color.DARKER_GREY));

		mc.fontRenderer.drawString(name1 + name2, guiLeft + 15, guiTop + 33, 0xFFFFFF);

		//Gui.drawRect(guiLeft + 6, guiTop + 46, right - 6, bottom - 30, Color.getValue(Color.GREY));

		textList.renderGuiBackground(mouseX, mouseY);

		if (editname) {
			int linex = guiLeft + 15 + mc.fontRenderer.getStringWidth(name1);
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				Gui.drawRect(linex, guiTop + 31, linex + 1, guiTop + 42, Color.getValue(Color.WHITE));
			}
		}
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0) {
			super.handleMouseInputSub();
		}
		if (wheel < 0) {
			textList.scrollUp();
		} else if (wheel > 0) {
			textList.scrollDown();
		}
	}

	private void handleRequest() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskMacroRequestPacket.class).setInteger(textList.getSelected()).setPosX(diskProvider.getX()).setPosY(diskProvider.getY()).setPosZ(diskProvider.getZ()));
	}

	private void handleDelete() {
		NBTTagCompound nbt = diskProvider.getDisk().getTagCompound();
		if (nbt == null) {
			diskProvider.getDisk().setTagCompound(new NBTTagCompound());
			nbt = diskProvider.getDisk().getTagCompound();
		}

		if (!nbt.hasKey("macroList")) {
			NBTTagList list = new NBTTagList();
			nbt.setTag("macroList", list);
		}

		NBTTagList list = nbt.getTagList("macroList", 10);
		NBTTagList listnew = new NBTTagList();

		for (int i = 0; i < list.tagCount(); i++) {
			if (i != textList.getSelected()) {
				listnew.appendTag(list.getCompoundTagAt(i));
			}
		}
		textList.setSelected(-1);
		nbt.setTag("macroList", listnew);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiscContent.class).setStack(diskProvider.getDisk()).setPosX(diskProvider.getX()).setPosY(diskProvider.getY()).setPosZ(diskProvider.getZ()));
	}

	private void handleAddEdit() {
		String macroname = "";
		NBTTagCompound nbt = diskProvider.getDisk().getTagCompound();
		if (nbt != null) {
			if (nbt.hasKey("macroList")) {
				NBTTagList list = nbt.getTagList("macroList", 10);
				if (textList.getSelected() != -1 && textList.getSelected() < list.tagCount()) {
					NBTTagCompound entry = list.getCompoundTagAt(textList.getSelected());
					macroname = entry.getString("name");
				}
			}
		}
		setSubGui(new GuiAddMacro(diskProvider, macroname));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) {
			handleRequest();
		} else if (guibutton.id == 1) {
			exitGui();
		} else if (guibutton.id == 2) {
			handleAddEdit();
		} else if (guibutton.id == 3) {
			handleDelete();
		} else if (guibutton.id == 4) {
			textList.scrollDown();
		} else if (guibutton.id == 5) {
			textList.scrollUp();
		} else {
			super.actionPerformed(guibutton);
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (editname) {
			if (c == 13) {
				writeDiskName();
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				name1 = name1 + GuiScreen.getClipboardString();
			} else if (c == 8) {
				if (name1.length() > 0) {
					name1 = name1.substring(0, name1.length() - 1);
				}
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (mc.fontRenderer.getStringWidth(name1 + c + name2) <= SEARCH_WIDTH) {
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
				writeDiskName();
			} else if (i == 28) { //Enter
				writeDiskName();
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
			//		} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
			//			super.keyTyped(c, i);
			//		} else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
			//			super.keyTyped(c, i);
		} else {
			super.keyTyped(c, i);
		}
	}
}
