package logisticspipes.gui.popup;

import logisticspipes.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.DiscContent;
import logisticspipes.network.packets.orderer.DiskMacroRequestPacket;
import logisticspipes.network.packets.orderer.DiskSetNamePacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.KraphtBaseGuiScreen.Colors;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiDiskPopup extends SubGuiScreen {
	
	private boolean editname = false;
	private boolean displaycursor = false;
	private long oldSystemTime = 0;
	private int mouseX = 0;
	private int mouseY = 0;
	private String name1;
	private String name2;
	private NormalMk2GuiOrderer mainGui;
	private int scroll = 0;
	private int selected = -1;
	
	private final int searchWidth = 120;
	
	public GuiDiskPopup(NormalMk2GuiOrderer mainGui) {
		super(150, 200, 0, 0);
		this.mainGui = mainGui;
		name2 = "";
		if(mainGui.getDisk().hasTagCompound()) {
			name1 = mainGui.getDisk().getTagCompound().getString("name");
		} else {
			name1 = "Disk";
		}
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		int x = i - guiLeft;
		int y = j - guiTop;
		mouseX = i;
		mouseY = j;
		if(k == 0) {
			if(10 < x && x < 138
			&& 29 < y && y < 44    ) {
				editname = true;
			} else if(editname) {
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
//TODO 	MainProxy.sendPacketToServer(new PacketPipeString(NetworkConstants.DISK_SET_NAME, mainGui.pipe.getX(), mainGui.pipe.getY(), mainGui.pipe.getZ(), name1 + name2).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskSetNamePacket.class).setString(name1 + name2).setPosX(mainGui.pipe.getX()).setPosY(mainGui.pipe.getY()).setPosZ(mainGui.pipe.getZ()));
		NBTTagCompound nbt = new NBTTagCompound("tag");
		if(mainGui.getDisk().hasTagCompound()) {
			nbt = mainGui.getDisk().getTagCompound();
		}
		nbt.setString("name", name1 + name2);
		mainGui.getDisk().setTagCompound(nbt);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, xCenter + 16	, bottom - 27, 50, 10, "Request"));
		buttonList.add(new SmallGuiButton(1, xCenter + 16	, bottom - 15, 50, 10, "Exit"));
		buttonList.add(new SmallGuiButton(2, xCenter - 66	, bottom - 27, 50, 10, "Add/Edit"));
		buttonList.add(new SmallGuiButton(3, xCenter - 66	, bottom - 15, 50, 10, "Delete"));
		buttonList.add(new SmallGuiButton(4, xCenter - 12	, bottom - 27, 25, 10, "/\\"));
		buttonList.add(new SmallGuiButton(5, xCenter - 12	, bottom - 15, 25, 10, "\\/"));
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		
		fontRenderer.drawStringWithShadow("Disk", xCenter - (fontRenderer.getStringWidth("Disk") / 2), guiTop + 10, 0xFFFFFF);
		
		//NameInput
		if(editname) {
			drawRect(guiLeft + 10, guiTop + 28, right - 10, guiTop + 45, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
			drawRect(guiLeft + 11, guiTop + 29, right - 11, guiTop + 44, BasicGuiHelper.ConvertEnumToColor(Colors.White));
		} else {
			drawRect(guiLeft + 11, guiTop + 29, right - 11, guiTop + 44, BasicGuiHelper.ConvertEnumToColor(Colors.Black));
		}
		drawRect(guiLeft + 12, guiTop + 30, right - 12, guiTop + 43, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
		
		fontRenderer.drawString(name1 + name2, guiLeft + 15, guiTop + 33, 0xFFFFFF);
		
		drawRect(guiLeft + 6, guiTop + 46, right - 6, bottom - 30, BasicGuiHelper.ConvertEnumToColor(Colors.MiddleGrey));
		
		NBTTagCompound nbt = mainGui.getDisk().getTagCompound();
		if(nbt == null) {
			mainGui.getDisk().setTagCompound(new NBTTagCompound("tag"));
			nbt = mainGui.getDisk().getTagCompound();
		}
		
		if(!nbt.hasKey("macroList")) {
			NBTTagList list = new NBTTagList();
			nbt.setTag("macroList", list);
		}
		
		NBTTagList list = nbt.getTagList("macroList");
		
		if(scroll + 12 > list.tagCount()) {
			scroll = list.tagCount() - 12;
		}
		if(scroll < 0) {
			scroll = 0;
		}
		
		boolean flag = false;
		
		if(guiLeft + 8 < mouseX && mouseX < right - 8 && guiTop + 48 < mouseY && mouseY < guiTop + 59 + (11 * 10)) {
			selected = scroll + (mouseY - guiTop - 49) / 10;
		}

		for(int i = scroll;i < list.tagCount() && (i - scroll) < 12;i++) {
			if(i == selected) {
				drawRect(guiLeft + 8, guiTop + 48 + ((i - scroll) * 10), right - 8, guiTop + 59 + ((i - scroll) * 10), BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey));
				flag = true;
			}
			NBTTagCompound entry = (NBTTagCompound) list.tagAt(i);
			String name = entry.getString("name");
			fontRenderer.drawString(name, guiLeft + 10, guiTop + 50 + ((i - scroll) * 10), 0xFFFFFF);
		}
		
		if(!flag) {
			selected = -1;
		}
		
		if(editname) {
			int linex = guiLeft + 15 + fontRenderer.getStringWidth(name1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, guiTop + 31, linex + 1, guiTop + 42, BasicGuiHelper.ConvertEnumToColor(Colors.White));
			}
		}
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	public void handleMouseInputSub() {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if(wheel == 0) super.handleMouseInputSub();
		if(wheel < 0) {
			scroll++;
		} else if(wheel > 0) {
			if(scroll > 0) {
				scroll--;
			}
		}
	}

	private void handleRequest() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.DISK_MACRO_REQUEST, mainGui.pipe.getX(), mainGui.pipe.getY(), mainGui.pipe.getZ(), selected).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskMacroRequestPacket.class).setInteger(selected).setPosX(mainGui.pipe.getX()).setPosY(mainGui.pipe.getY()).setPosZ(mainGui.pipe.getZ()));
	}

	private void handleDelete() {
		NBTTagCompound nbt = mainGui.getDisk().getTagCompound();
		if(nbt == null) {
			mainGui.getDisk().setTagCompound(new NBTTagCompound("tag"));
			nbt = mainGui.getDisk().getTagCompound();
		}

		if(!nbt.hasKey("macroList")) {
			NBTTagList list = new NBTTagList();
			nbt.setTag("macroList", list);
		}

		NBTTagList list = nbt.getTagList("macroList");
		NBTTagList listnew = new NBTTagList();

		for(int i = 0;i < list.tagCount();i++) {
			if(i != selected) {
				listnew.appendTag(list.tagAt(i));
			}
		}
		selected = -1;
		nbt.setTag("macroList", listnew);
//TODO 	MainProxy.sendPacketToServer(new PacketItem(NetworkConstants.DISK_CONTENT, mainGui.pipe.getX(), mainGui.pipe.getY(), mainGui.pipe.getZ(), mainGui.pipe.getDisk()).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiscContent.class).setStack(mainGui.pipe.getDisk()).setPosX(mainGui.pipe.getX()).setPosY(mainGui.pipe.getY()).setPosZ(mainGui.pipe.getZ()));
	}

	private void handleAddEdit() {
		String macroname = "";
		NBTTagCompound nbt = mainGui.getDisk().getTagCompound();
		if(nbt != null) {
			if(nbt.hasKey("macroList")) {
				NBTTagList list = nbt.getTagList("macroList");
				if(selected != -1 && selected < list.tagCount()) {
					NBTTagCompound entry = (NBTTagCompound) list.tagAt(selected);
					macroname = entry.getString("name");
				}
			}
		}
		this.setSubGui(new GuiAddMacro(mainGui, macroname));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0) {
			handleRequest();
		} else if (guibutton.id == 1) {
			this.exitGui();
		} else if (guibutton.id == 2) {
			handleAddEdit();
		} else if (guibutton.id == 3) {
			handleDelete();
		} else if (guibutton.id == 4) {
			if(scroll > 0) {
				scroll--;
			}
		} else if (guibutton.id == 5) {
			scroll++;
		} else {
			super.actionPerformed(guibutton);
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if(editname) {
			if (c == 13) {
				writeDiskName();
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				name1 = name1 + getClipboardString();
			} else if (c == 8) {
				if (name1.length() > 0)
					name1 = name1.substring(0, name1.length() - 1);
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (fontRenderer.getStringWidth(name1 + c + name2) <= searchWidth) {
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
				writeDiskName();
			} else if(i == 28) { //Enter
				writeDiskName();
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
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
//			super.keyTyped(c, i);
//		} else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
//			super.keyTyped(c, i);
		} else {
			super.keyTyped(c, i);
		}
	}
}
