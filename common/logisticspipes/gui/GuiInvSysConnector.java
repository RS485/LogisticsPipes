package logisticspipes.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IGUIChannelInformationReceiver;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.InvSysConContentRequest;
import logisticspipes.network.packets.pipe.InvSysConOpenSelectChannelPopupPacket;
import logisticspipes.network.packets.pipe.InvSysConResistance;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiInvSysConnector extends LogisticsBaseGuiScreen implements IGUIChannelInformationReceiver {

	private static final String PREFIX = "gui.invsyscon.";

	private int page = 0;
	private final List<ItemIdentifierStack> _allItems = new ArrayList<>();
	private final PipeItemsInvSysConnector pipe;
	private InputBar resistanceCountBar;

	private ChannelInformation connectedChannel = null;

	public GuiInvSysConnector(EntityPlayer player, PipeItemsInvSysConnector pipe) {
		super(180, 220, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, null);

		dummy.addNormalSlotsForPlayerInventory(10, 135);

		inventorySlots = dummy;
		this.pipe = pipe;

	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 120, guiTop + 67, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 160, guiTop + 67, 10, 10, ">"));
		buttonList.add(new SmallGuiButton(2, guiLeft + 68, guiTop + 67, 46, 10, TextUtil.translate(GuiInvSysConnector.PREFIX + "Refresh")));
		buttonList.add(new SmallGuiButton(3, guiLeft + 80, guiTop + 55, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(4, guiLeft + 120, guiTop + 55, 10, 10, ">"));
		buttonList.add(new SmallGuiButton(5, guiLeft + 140, guiTop + 55, 30, 10, TextUtil.translate(GuiInvSysConnector.PREFIX + "Save")));
		buttonList.add(new SmallGuiButton(6, guiLeft + 130, guiTop + 20, 40, 10, TextUtil.translate(GuiInvSysConnector.PREFIX + "Change")));

		if (this.resistanceCountBar == null) {
			this.resistanceCountBar = new InputBar(this.fontRenderer, this, guiLeft + 90, guiTop + 55, 30, 12, false, true, InputBar.Align.CENTER);
			this.resistanceCountBar.minNumber = 0;
			this.resistanceCountBar.setInteger(pipe.resistance);
		}
		this.resistanceCountBar.reposition(guiLeft + 90, guiTop + 55, 30, 12);

		refreshPacket();
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 135);
		mc.fontRenderer.drawString(TextUtil.translate(GuiInvSysConnector.PREFIX + "InventorySystemConnector"), guiLeft + 5, guiTop + 6, 0x404040);
		drawRect(guiLeft + 9, guiTop + 78, guiLeft + 170, guiTop + 132, Color.GREY);
		mc.fontRenderer.drawString(TextUtil.translate(GuiInvSysConnector.PREFIX + "ConnectionInformation") + ":", guiLeft + 10, guiTop + 21, 0x404040);
		mc.fontRenderer.drawString(TextUtil.getTrimmedString(TextUtil.translate(GuiInvSysConnector.PREFIX + "Channel") + ": " + (connectedChannel != null ? connectedChannel.getName() : "UNDEFINED"), 150, this.fontRenderer, "..."), guiLeft + 15, guiTop + 38, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiInvSysConnector.PREFIX + "Waitingfor") + ":", guiLeft + 10, guiTop + 68, 0x404040);
		mc.fontRenderer.drawString((page + 1) + "/" + maxPage(), guiLeft + 136, guiTop + 69, 0x404040);
		mc.fontRenderer.drawString(TextUtil.translate(GuiInvSysConnector.PREFIX + "Resistance") + ":", guiLeft + 10, guiTop + 55, 0x404040);
		resistanceCountBar.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(_allItems, null, page, 9, 79, 9, 27, 18, 18, 100.0F, DisplayAmount.ALWAYS);

		int ppi = 0;
		int column = 0;
		int row = 0;
		for (ItemIdentifierStack itemStack : _allItems) {
			ppi++;

			if (ppi <= 27 * page) {
				continue;
			}
			if (ppi > 27 * (page + 1)) {
				continue;
			}
			ItemStack st = itemStack.unsafeMakeNormalStack();
			int x = 9 + 18 * column + guiLeft;
			int y = 79 + 18 * row + guiTop;

			GL11.glDisable(2896 /*GL_LIGHTING*/);

			int mouseX = Mouse.getX() * width / mc.displayWidth;
			int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

			if (x < mouseX && mouseX < x + 18 && y < mouseY && mouseY < y + 18) {
				GuiGraphics.displayItemToolTip(new Object[] { mouseX, mouseY, st, true }, zLevel, guiLeft, guiTop, false);
			}

			column++;
			if (column >= 9) {
				row++;
				column = 0;
			}
		}
	}

	private void refreshPacket() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(InvSysConContentRequest.class).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
	}

	private void pageDown() {
		if (page <= 0) {
			page = maxPage() - 1;
		} else {
			page--;
		}
	}

	private void pageUp() {
		if (page >= maxPage() - 1) {
			page = 0;
		} else {
			page++;
		}
	}

	private int maxPage() {
		int i = (int) (Math.floor(((float) _allItems.size()) / 27) + (((float) _allItems.size()) % 27 == 0 ? 0 : 1));
		if (i <= 0) {
			i = 1;
		}
		return i;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 0) {
			pageDown();
		} else if (button.id == 1) {
			pageUp();
		} else if (button.id == 2) {
			refreshPacket();
		} else if (button.id == 3) {
			resistanceCountBar.setInteger(resistanceCountBar.getInteger() - (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 10 : 1));
		} else if (button.id == 4) {
			resistanceCountBar.setInteger(resistanceCountBar.getInteger() + 1);
		} else if (button.id == 5) {
			pipe.resistance = resistanceCountBar.getInteger();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(InvSysConResistance.class).setInteger(pipe.resistance).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
		} else if (button.id == 6) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(InvSysConOpenSelectChannelPopupPacket.class).setTilePos(pipe.container));
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int k) throws IOException {
		if (!resistanceCountBar.handleClick(x, y, k)) {
			super.mouseClicked(x, y, k);
		}
	}

	@Override
	public void keyTyped(char c, int i) throws IOException {
		if (!resistanceCountBar.handleKey(c, i)) {
			super.keyTyped(c, i);
		}
	}

	public void handleContentAnswer(Collection<ItemIdentifierStack> allItems) {
		_allItems.clear();
		_allItems.addAll(allItems);
	}

	public void handleResistanceAnswer(int resistance) {
		resistanceCountBar.setInteger(resistance);
	}

	@Override
	public void handleChannelInformation(ChannelInformation channel, boolean flag) {
		if (this.getSubGui() instanceof IGUIChannelInformationReceiver) {
			((IGUIChannelInformationReceiver) this.getSubGui()).handleChannelInformation(channel, flag);
		}
		if (flag) {
			this.connectedChannel = channel;
		} else if (this.connectedChannel != null && this.connectedChannel.getChannelIdentifier().equals(channel.getChannelIdentifier())) {
			this.connectedChannel = channel;
		}
	}
}
