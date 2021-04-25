package logisticspipes.gui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import org.lwjgl.input.Keyboard;

import logisticspipes.LogisticsPipes;
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
import logisticspipes.network.packets.gui.OpenSecurityChannelManagerPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiSecurityStation extends LogisticsBaseGuiScreen implements PlayerListReciver {

	private static final String PREFIX = "gui.securitystation.";

	private final LogisticsSecurityTileEntity _tile;
	private final List<String> players = new LinkedList<>();

	//Player name:
	protected static final int searchWidth = 250;
	protected int lastClickedx = 0;
	protected int lastClickedy = 0;
	protected int lastClickedk = 0;
	private int addition;
	private boolean authorized;
	private InputBar searchBar;

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

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiLeft + 10, guiTop + 179, 30, 20, "--"));
		buttonList.get(0).visible = false;
		buttonList.add(new GuiButton(1, guiLeft + 10, guiTop + 139, 30, 20, "-"));
		buttonList.add(new GuiButton(2, guiLeft + 45, guiTop + 139, 30, 20, "+"));
		buttonList.add(new GuiButton(3, guiLeft + 140, guiTop + 179, 30, 20, "++"));
		buttonList.get(3).visible = false;
		buttonList.add(new SmallGuiButton(4, guiLeft + 241, guiTop + 217, 30, 10, TextUtil
				.translate(GuiSecurityStation.PREFIX + "Open")));
		buttonList.add(new GuiCheckBox(5, guiLeft + 160, guiTop + 42, 16, 16, _tile.allowCC));
		buttonList.add(new SmallGuiButton(6, guiLeft + 162, guiTop + 60, 60, 10, TextUtil.translate(GuiSecurityStation.PREFIX + "EditTable")));
		if (!SimpleServiceLocator.ccProxy.isCC() && !LogisticsPipes.isDEBUG()) {
			buttonList.get(5).visible = false;
			buttonList.get(6).visible = false;
		}
		buttonList.add(new GuiButton(7, guiLeft + 55, guiTop + 95, 70, 20, TextUtil.translate(GuiSecurityStation.PREFIX + "Authorize")));
		buttonList.add(new GuiButton(8, guiLeft + 175, guiTop + 95, 70, 20, TextUtil.translate(GuiSecurityStation.PREFIX + "Deauthorize")));
		buttonList.add(new GuiCheckBox(9, guiLeft + 160, guiTop + 74, 16, 16, _tile.allowAutoDestroy));
		buttonList.add(new GuiButton(10, guiLeft + 177, guiTop + 230, 95, 20, TextUtil.translate(GuiSecurityStation.PREFIX + "ChannelManager")));
		if (searchBar == null) {
			searchBar = new InputBar(this.fontRenderer, this, guiLeft + 180, bottom - 120, right - 8 + addition - guiLeft - 180, 17);
			lastClickedx = -10000000;
			lastClickedy = -10000000;
		}
		searchBar.reposition(guiLeft + 180, bottom - 120, right - 8 + addition - guiLeft - 180, 17);
		MainProxy.sendPacketToServer(PacketHandler.getPacket(PlayerListRequest.class));
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 4) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityCardPacket.class).setInteger(button.id).setBlockPos(_tile.getPos()));
		} else if (button.id == 4) {
			if (!searchBar.getText().isEmpty()) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityStationOpenPlayerRequest.class).setString(searchBar.getText()).setBlockPos(_tile.getPos()));
			}
		} else if (button.id == 5) {
			_tile.allowCC = !_tile.allowCC;
			refreshCheckBoxes();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityStationCC.class).setInteger(_tile.allowCC ? 1 : 0).setBlockPos(_tile.getPos()));
		} else if (button.id == 6) {
			setSubGui(new GuiEditCCAccessTable(_tile));
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityRequestCCIdsPacket.class).setBlockPos(_tile.getPos()));
		} else if (button.id == 7) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityAuthorizationPacket.class).setInteger(1).setBlockPos(_tile.getPos()));
			authorized = true;
		} else if (button.id == 8) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityAuthorizationPacket.class).setInteger(0).setBlockPos(_tile.getPos()));
			authorized = false;
		} else if (button.id == 9) {
			_tile.allowAutoDestroy = !_tile.allowAutoDestroy;
			refreshCheckBoxes();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SecurityStationAutoDestroy.class).setInteger(_tile.allowAutoDestroy ? 1 : 0).setBlockPos(_tile.getPos()));
		} else if (button.id == 10) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(OpenSecurityChannelManagerPacket.class).setBlockPos(_tile.getPos()));
		} else {
			super.actionPerformed(button);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 175);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 81, guiTop + 140);
		mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "SecurityStation"), guiLeft + 105, guiTop + 10, 0x404040);
		mc.fontRenderer.drawString(_tile.getSecId() == null ? "null" : _tile.getSecId().toString(), guiLeft + 32, guiTop + 25, 0x404040);
		if (SimpleServiceLocator.ccProxy.isCC() || LogisticsPipes.isDEBUG()) {
			mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "allowCCAccess") + ":", guiLeft + 10, guiTop + 46, 0x404040);
			mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "excludeIDs") + ":", guiLeft + 10, guiTop + 61, 0x404040);
		}
		mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "pipeRemove") + ":", guiLeft + 10, guiTop + 78, 0x404040);
		//mc.fontRenderer.drawString("---------------------------------------------", guiLeft + 5, guiTop + 90, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "Player") + ":", guiLeft + 180, guiTop + 127, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "SecurityCards") + ":", guiLeft + 10, guiTop + 127, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiSecurityStation.PREFIX + "Inventory") + ":", guiLeft + 10, guiTop + 163, 0x404040);

		addition = (mc.fontRenderer.getStringWidth(searchBar.getText()) - 82);

		if (addition < 0) {
			addition = 0;
		}

		searchBar.drawTextBox();

		int pos = bottom - 95;
		for (String player : players) {
			if (player.contains(searchBar.getText())) {
				mc.fontRenderer.drawString(player, guiLeft + 180, pos, 0x404040);
				pos += 11;
			}
			//Check mouse click
			if (guiLeft + 180 < lastClickedx && lastClickedx < guiLeft + 280 && pos - 11 < lastClickedy && lastClickedy < pos) {
				lastClickedx = -10000000;
				lastClickedy = -10000000;
				searchBar.setText(player);
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
	protected void mouseClicked(int i, int j, int k) throws IOException {
		if (searchBar.handleClick(i, j, k))
			return;
		super.mouseClicked(i, j, k);

		if ((i >= guiLeft + 5 && i < right - 5 + addition && j >= guiTop + 5 && j < bottom - 5) && !searchBar.isFocused()) {
			lastClickedx = i;
			lastClickedy = j;
			lastClickedk = k;
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (searchBar.isFocused()) {
			if ((c == 13) || (i == 1) || (i == 28)) {
				searchBar.setFocused(false);
				return;
			}
			if (searchBar.handleKey(c, i))
				return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void recivePlayerList(List<String> list) {
		players.clear();
		players.addAll(list);
	}

	public void handlePlayerSecurityOpen(SecuritySettings setting) {
		searchBar.setText("");
		setSubGui(new GuiSecurityStationPopup(setting, _tile));
	}

	public void refreshCheckBoxes() {
		((GuiCheckBox) buttonList.get(5)).setState(_tile.allowCC);
		((GuiCheckBox) buttonList.get(9)).setState(_tile.allowAutoDestroy);
	}
}
