package logisticspipes.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.interfaces.PlayerListReciver;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.LogisticsPipesPacket;
import logisticspipes.network.packets.PacketNBT;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.input.Keyboard;

public class GuiSecurityStation extends KraphtBaseGuiScreen implements PlayerListReciver {
	
	private final LogisticsSecurityTileEntity _tile;
	private final List<String> players = new LinkedList<String>();
	
	private SecuritySettings activeSetting = null;
	
	//Player name:
	protected String searchinput1 = "";
	protected String searchinput2 = "";
	protected boolean editsearch = false;
	protected boolean editsearchb = false;
	protected boolean displaycursor = true;
	protected long oldSystemTime = 0;
	protected static final int searchWidth = 250;
	protected int lastClickedx = 0;
	protected int lastClickedy = 0;
	protected int lastClickedk = 0;
	private int addition;
	
	protected final String _title = "Request items";
	protected boolean clickWasButton = false;
	
	
	public GuiSecurityStation(LogisticsSecurityTileEntity tile, EntityPlayer player) {
		super(280, 300, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, tile.inv);
		dummy.addRestrictedSlot(0, tile.inv, 82, 181, -1);
		dummy.addNormalSlotsForPlayerInventory(10, 215);
		this.inventorySlots = dummy;
		_tile = tile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, guiLeft + 10, guiTop + 179, 30, 20, "--"));
		this.buttonList.add(new GuiButton(1, guiLeft + 45, guiTop + 179, 30, 20, "-"));
		this.buttonList.add(new GuiButton(2, guiLeft + 105, guiTop + 179, 30, 20, "+"));
		this.buttonList.add(new GuiButton(3, guiLeft + 140, guiTop + 179, 30, 20, "++"));
		this.buttonList.add(new SmallGuiButton(4, guiLeft + 241, guiTop + 167, 30, 10, "Open"));
		this.buttonList.add(new SmallGuiButton(5, guiLeft + 184, guiTop + 114, 30, 10, "Save"));
		this.buttonList.add(new GuiCheckBox(6, guiLeft + 200, guiTop + 51, 16, 16, false));
		this.buttonList.add(new GuiCheckBox(7, guiLeft + 200, guiTop + 66, 16, 16, false));
		this.buttonList.add(new GuiCheckBox(8, guiLeft + 200, guiTop + 81, 16, 16, false));
		this.buttonList.add(new GuiCheckBox(9, guiLeft + 200, guiTop + 96, 16, 16, false));
		if(SimpleServiceLocator.ccProxy.isCC() || LogisticsPipes.DEBUG) {
			this.buttonList.add(new GuiCheckBox(10, guiLeft + 160, guiTop + 142, 16, 16, _tile.allowCC));
		}
		MainProxy.sendPacketToServer(new LogisticsPipesPacket() {
			@Override public void writeData(DataOutputStream data) throws IOException {}
			@Override public void readData(DataInputStream data) throws IOException {}
			@Override public int getID() {
				return NetworkConstants.PLAYER_LIST;
			}
		}.getPacket());
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id < 4) {
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.SECURITY_CARD, _tile.xCoord, _tile.yCoord, _tile.zCoord, button.id).getPacket());
		} else if(button.id == 4) {
			MainProxy.sendPacketToServer(new PacketStringCoordinates(NetworkConstants.OPEN_SECURITY_PLAYER, _tile.xCoord, _tile.yCoord, _tile.zCoord, searchinput1 + searchinput2).getPacket());	
		} else if(button.id == 5) {
			NBTTagCompound nbt = new NBTTagCompound();
			activeSetting.writeToNBT(nbt);
			MainProxy.sendPacketToServer(new PacketNBT(NetworkConstants.SAVE_SECURITY_PLAYER, _tile.xCoord, _tile.yCoord, _tile.zCoord, nbt).getPacket());
		} else if(button.id == 6) {
			activeSetting.openGui = !activeSetting.openGui;
			refreshCheckBoxes();
		} else if(button.id == 7) {
			activeSetting.openRequest = !activeSetting.openRequest;
			refreshCheckBoxes();
		} else if(button.id == 8) {
			activeSetting.openUpgrades = !activeSetting.openUpgrades;
			refreshCheckBoxes();
		} else if(button.id == 9) {
			activeSetting.openNetworkMonitor = !activeSetting.openNetworkMonitor;
			refreshCheckBoxes();
		} else if(button.id == 10) {
			_tile.allowCC = !_tile.allowCC;
			refreshCheckBoxes();
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.SET_SECURITY_CC, _tile.xCoord, _tile.yCoord, _tile.zCoord, _tile.allowCC?1:0).getPacket());
		} else {
			super.actionPerformed(button);
		}
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Security_Station_ID;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 215);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 81, guiTop + 180);
		fontRenderer.drawString("Security Station", guiLeft + 105, guiTop + 10, 0x404040);
		fontRenderer.drawString(_tile.getSecId() == null ? "null" : _tile.getSecId().toString(), guiLeft + 32, guiTop + 25, 0x404040);
		fontRenderer.drawString("Player:", guiLeft + 180, guiTop + 167, 0x404040);
		fontRenderer.drawString("Inventory:", guiLeft + 10, guiTop + 203, 0x404040);
		fontRenderer.drawString("Security Cards:", guiLeft + 10, guiTop + 167, 0x404040);
		if(SimpleServiceLocator.ccProxy.isCC() || LogisticsPipes.DEBUG) {
			fontRenderer.drawString("Allow ComputerCraft Access:", guiLeft + 10, guiTop + 147, 0x404040);
		}
		
		addition = (fontRenderer.getStringWidth(searchinput1 + searchinput2) - 82);
		
		if(addition < 0) {
			addition = 0;
		}
		
		//SearchInput
		if(editsearch) {
			drawRect(guiLeft + 180, bottom - 120, right - 8 + addition, bottom - 103, Colors.Black);
			drawRect(guiLeft + 181, bottom - 119, right - 9 + addition, bottom - 104, Colors.White);
		} else {
			drawRect(guiLeft + 181, bottom - 119, right - 9 + addition, bottom - 104, Colors.Black);
		}
		drawRect(guiLeft + 182, bottom - 118, right - 10 + addition, bottom - 105, Colors.DarkGrey);
		
		fontRenderer.drawString(searchinput1 + searchinput2, guiLeft + 185, bottom - 115, 0xFFFFFF);
		if(editsearch) {
			int linex = guiLeft + 185 + fontRenderer.getStringWidth(searchinput1);
			if(System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if(displaycursor) {
				drawRect(linex, bottom - 117, linex + 1, bottom - 106, Colors.White);
			}
		}
		
		//Click into search
		if(lastClickedx != -10000000 &&	lastClickedy != -10000000) {
			if (lastClickedx >= guiLeft + 182 && lastClickedx < right - 8 + addition &&
					lastClickedy >= bottom - 120 && lastClickedy < bottom - 102){
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
		
		int pos = bottom - 95;
		for(String player:players) {
			if(player.contains(searchinput1 + searchinput2)) {
				fontRenderer.drawString(player, guiLeft + 180, pos, 0x404040);
				pos += 11;
			}
			//Check mouse click
			if(guiLeft + 180 < lastClickedx && lastClickedx < guiLeft + 280 && pos - 11 < lastClickedy && lastClickedy < pos) {
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				searchinput1 = player;
				searchinput2 = "";
			}
			if(pos > bottom - 12) {
				fontRenderer.drawString("...", guiLeft + 180, pos - 5, 0x404040);
				break;
			}
		}
		
		if(activeSetting != null) {
			fontRenderer.drawString("Player: " + activeSetting.name, guiLeft + 10, guiTop + 40, 0x404040);
			fontRenderer.drawString("Allow Open Pipe Settings Gui: ", guiLeft + 55, guiTop + 55, 0x404040);
			fontRenderer.drawString("Allow Item Requesting: ", guiLeft + 87, guiTop + 70, 0x404040);
			fontRenderer.drawString("Allow Open Pipe Upgrades Gui: ", guiLeft + 47, guiTop + 85, 0x404040);
			fontRenderer.drawString("Allow Open Pipe Network Manager Gui: ", guiLeft + 10, guiTop + 100, 0x404040);
			((GuiButton)this.buttonList.get(5)).drawButton = true;
			((GuiButton)this.buttonList.get(6)).drawButton = true;
			((GuiButton)this.buttonList.get(7)).drawButton = true;
			((GuiButton)this.buttonList.get(8)).drawButton = true;
			((GuiButton)this.buttonList.get(9)).drawButton = true;
		} else {
			((GuiButton)this.buttonList.get(5)).drawButton = false;
			((GuiButton)this.buttonList.get(6)).drawButton = false;
			((GuiButton)this.buttonList.get(7)).drawButton = false;
			((GuiButton)this.buttonList.get(8)).drawButton = false;
			((GuiButton)this.buttonList.get(9)).drawButton = false;
		}
	}

	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		clickWasButton = false;
		editsearchb = true;
		super.mouseClicked(i, j, k);
		if ((!clickWasButton && i >= guiLeft + 5 && i < right - 5 + addition && j >= guiTop + 5 && j < bottom - 5) || editsearch){
			if(!editsearchb) {
				editsearch = false;
			}
			lastClickedx = i;
			lastClickedy = j;
			lastClickedk = k;
		}
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
		} else {
			super.keyTyped(c, i);
		}
	}
	
	@Override
	public void recivePlayerList(List<String> list) {
		players.clear();
		players.addAll(list);
	}

	public void handlePlayerSecurityOpen(SecuritySettings setting) {
		activeSetting = setting;
		refreshCheckBoxes();
	}
	
	public void refreshCheckBoxes() {
		if(activeSetting != null) {
			((GuiCheckBox)this.buttonList.get(6)).setState(activeSetting.openGui);
			((GuiCheckBox)this.buttonList.get(7)).setState(activeSetting.openRequest);
			((GuiCheckBox)this.buttonList.get(8)).setState(activeSetting.openUpgrades);
			((GuiCheckBox)this.buttonList.get(9)).setState(activeSetting.openNetworkMonitor);
		}
		((GuiCheckBox)this.buttonList.get(10)).setState(_tile.allowCC);
	}
}
