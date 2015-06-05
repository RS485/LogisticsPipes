package logisticspipes.gui;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.gui.popup.GuiEditCCAccessTable;
import logisticspipes.gui.popup.GuiSecurityStationPopup;
import logisticspipes.interfaces.PlayerListReciver;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerListRequest;
import logisticspipes.network.packets.block.SecurityAuthorizationPacket;
import logisticspipes.network.packets.block.SecurityCardPacket;
import logisticspipes.network.packets.block.SecurityRequestCCIdsPacket;
import logisticspipes.network.packets.block.SecurityStationAutoDestroy;
import logisticspipes.network.packets.block.SecurityStationCC;
import logisticspipes.network.packets.block.SecurityStationOpenPlayerRequest;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import org.lwjgl.input.Keyboard;

public class GuiSecurityStation extends LogisticsBaseGuiScreen implements PlayerListReciver {

	private static final String PREFIX = "gui.securitystation.";

	private final LogisticsSecurityTileEntity _tile;
	private final List<String> players = new LinkedList<String>();

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
	private boolean authorized;

	protected final String _title = "Request items";
	protected boolean clickWasButton = false;

	public GuiSecurityStation(LogisticsSecurityTileEntity tile, EntityPlayer player) {
		super(280, 260, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, tile.inv);
		dummy.addRestrictedSlot(0, tile.inv, 82, 141, (Item) null);
		dummy.addNormalSlotsForPlayerInventory(10, 175);
		inventorySlots = dummy;
		_tile = tile;
		authorized = SimpleServiceLocator.securityStationManager.isAuthorized(tile.getSecId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiLeft + 10, guiTop + 179, 30, 20, "--"));
		((GuiButton) buttonList.get(0)).visible = false;
		buttonList.add(new GuiButton(1, guiLeft + 10, guiTop + 139, 30, 20, "-"));
		buttonList.add(new GuiButton(2, guiLeft + 45, guiTop + 139, 30, 20, "+"));
		buttonList.add(new GuiButton(3, guiLeft + 140, guiTop + 179, 30, 20, "++"));
		((GuiButton) buttonList.get(3)).visible = false;
		buttonList.add(new SmallGuiButton(4, guiLeft + 241, guiTop + 217, 30, 10, StringUtils.translate(GuiSecurityStation.PREFIX + "Open")));
		buttonList.add(new GuiCheckBox(5, guiLeft + 160, guiTop + 42, 16, 16, _tile.allowCC));
		buttonList.add(new SmallGuiButton(6, guiLeft + 162, guiTop + 60, 60, 10, StringUtils.translate(GuiSecurityStation.PREFIX + "EditTable")));
		if (!SimpleServiceLocator.ccProxy.isCC() && !LPConstants.DEBUG) {
			((GuiButton) buttonList.get(5)).visible = false;
			((GuiButton) buttonList.get(6)).visible = false;
		}
		buttonList.add(new GuiButton(7, guiLeft + 55, guiTop + 95, 70, 20, StringUtils.translate(GuiSecurityStation.PREFIX + "Authorize")));
		buttonList.add(new GuiButton(8, guiLeft + 175, guiTop + 95, 70, 20, StringUtils.translate(GuiSecurityStation.PREFIX + "Deauthorize")));
		buttonList.add(new GuiCheckBox(9, guiLeft + 160, guiTop + 74, 16, 16, _tile.allowAutoDestroy));
		MainProxy.sendPacketToServer(PacketHandler.getPacket(PlayerListRequest.class));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id < 4) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityCardPacket.class).setInteger(button.id).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
		} else if (button.id == 4) {
			if (searchinput1 + searchinput2 != null && ((searchinput1 + searchinput2).length() != 0)) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityStationOpenPlayerRequest.class).setString(searchinput1 + searchinput2).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
			}
		} else if (button.id == 5) {
			_tile.allowCC = !_tile.allowCC;
			refreshCheckBoxes();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityStationCC.class).setInteger(_tile.allowCC ? 1 : 0).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
		} else if (button.id == 6) {
			setSubGui(new GuiEditCCAccessTable(_tile));
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityRequestCCIdsPacket.class).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
		} else if (button.id == 7) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityAuthorizationPacket.class).setInteger(1).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
			authorized = true;
		} else if (button.id == 8) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityAuthorizationPacket.class).setInteger(0).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
			authorized = false;
		} else if (button.id == 9) {
			_tile.allowAutoDestroy = !_tile.allowAutoDestroy;
			refreshCheckBoxes();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityStationAutoDestroy.class).setInteger(_tile.allowAutoDestroy ? 1 : 0).setPosX(_tile.xCoord).setPosY(_tile.yCoord).setPosZ(_tile.zCoord));
		} else {
			super.actionPerformed(button);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 175);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 81, guiTop + 140);
		mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "SecurityStation"), guiLeft + 105, guiTop + 10, 0x404040);
		mc.fontRenderer.drawString(_tile.getSecId() == null ? "null" : _tile.getSecId().toString(), guiLeft + 32, guiTop + 25, 0x404040);
		if (SimpleServiceLocator.ccProxy.isCC() || LPConstants.DEBUG) {
			mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "allowCCAccess") + ":", guiLeft + 10, guiTop + 46, 0x404040);
			mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "excludeIDs") + ":", guiLeft + 10, guiTop + 61, 0x404040);
		}
		mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "pipeRemove") + ":", guiLeft + 10, guiTop + 78, 0x404040);
		//mc.fontRenderer.drawString("---------------------------------------------", guiLeft + 5, guiTop + 90, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "Player") + ":", guiLeft + 180, guiTop + 127, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "SecurityCards") + ":", guiLeft + 10, guiTop + 127, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiSecurityStation.PREFIX + "Inventory") + ":", guiLeft + 10, guiTop + 163, 0x404040);

		addition = (mc.fontRenderer.getStringWidth(searchinput1 + searchinput2) - 82);

		if (addition < 0) {
			addition = 0;
		}

		//SearchInput
		if (editsearch) {
			drawRect(guiLeft + 180, bottom - 120, right - 8 + addition, bottom - 103, Color.BLACK);
			drawRect(guiLeft + 181, bottom - 119, right - 9 + addition, bottom - 104, Color.WHITE);
		} else {
			drawRect(guiLeft + 181, bottom - 119, right - 9 + addition, bottom - 104, Color.BLACK);
		}
		drawRect(guiLeft + 182, bottom - 118, right - 10 + addition, bottom - 105, Color.DARKER_GREY);

		mc.fontRenderer.drawString(searchinput1 + searchinput2, guiLeft + 185, bottom - 115, 0xFFFFFF);
		if (editsearch) {
			int linex = guiLeft + 185 + mc.fontRenderer.getStringWidth(searchinput1);
			if (System.currentTimeMillis() - oldSystemTime > 500) {
				displaycursor = !displaycursor;
				oldSystemTime = System.currentTimeMillis();
			}
			if (displaycursor) {
				drawRect(linex, bottom - 117, linex + 1, bottom - 106, Color.WHITE);
			}
		}

		//Click into search
		if (lastClickedx != -10000000 && lastClickedy != -10000000) {
			if (lastClickedx >= guiLeft + 182 && lastClickedx < right - 8 + addition && lastClickedy >= bottom - 120 && lastClickedy < bottom - 102) {
				editsearch = true;
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				if (lastClickedk == 1) {
					searchinput1 = "";
					searchinput2 = "";
				}
			} else {
				editsearch = false;
			}
		}

		int pos = bottom - 95;
		for (String player : players) {
			if (player.contains(searchinput1 + searchinput2)) {
				mc.fontRenderer.drawString(player, guiLeft + 180, pos, 0x404040);
				pos += 11;
			}
			//Check mouse click
			if (guiLeft + 180 < lastClickedx && lastClickedx < guiLeft + 280 && pos - 11 < lastClickedy && lastClickedy < pos) {
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				searchinput1 = player;
				searchinput2 = "";
			}
			if (pos > bottom - 12) {
				mc.fontRenderer.drawString("...", guiLeft + 180, pos - 5, 0x404040);
				break;
			}
		}
		if (authorized) {
			Gui.drawRect(guiLeft + 127, guiTop + 101, guiLeft + 147, guiTop + 108, Color.getValue(Color.GREEN));
		} else {
			Gui.drawRect(guiLeft + 153, guiTop + 101, guiLeft + 173, guiTop + 108, Color.getValue(Color.RED));
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		clickWasButton = false;
		editsearchb = true;
		super.mouseClicked(i, j, k);
		if ((!clickWasButton && i >= guiLeft + 5 && i < right - 5 + addition && j >= guiTop + 5 && j < bottom - 5) || editsearch) {
			if (!editsearchb) {
				editsearch = false;
			}
			lastClickedx = i;
			lastClickedy = j;
			lastClickedk = k;
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (editsearch) {
			if (c == 13) {
				editsearch = false;
				return;
			} else if (i == 47 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				searchinput1 = searchinput1 + GuiScreen.getClipboardString();
			} else if (c == 8) {
				if (searchinput1.length() > 0) {
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				}
				return;
			} else if (Character.isLetterOrDigit(c) || c == ' ') {
				if (mc.fontRenderer.getStringWidth(searchinput1 + c + searchinput2) <= GuiSecurityStation.searchWidth) {
					searchinput1 += c;
				}
				return;
			} else if (i == 203) { //Left
				if (searchinput1.length() > 0) {
					searchinput2 = searchinput1.substring(searchinput1.length() - 1) + searchinput2;
					searchinput1 = searchinput1.substring(0, searchinput1.length() - 1);
				}
			} else if (i == 205) { //Right
				if (searchinput2.length() > 0) {
					searchinput1 += searchinput2.substring(0, 1);
					searchinput2 = searchinput2.substring(1);
				}
			} else if (i == 1) { //ESC
				editsearch = false;
			} else if (i == 28) { //Enter
				editsearch = false;
			} else if (i == 199) { //Pos
				searchinput2 = searchinput1 + searchinput2;
				searchinput1 = "";
			} else if (i == 207) { //Ende
				searchinput1 = searchinput1 + searchinput2;
				searchinput2 = "";
			} else if (i == 211) { //Entf
				if (searchinput2.length() > 0) {
					searchinput2 = searchinput2.substring(1);
				}
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
		searchinput1 = "";
		searchinput2 = "";
		setSubGui(new GuiSecurityStationPopup(setting, _tile));
	}

	public void refreshCheckBoxes() {
		((GuiCheckBox) buttonList.get(5)).setState(_tile.allowCC);
		((GuiCheckBox) buttonList.get(9)).setState(_tile.allowAutoDestroy);
	}
}
