package logisticspipes.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.InvSysConContentRequest;
import logisticspipes.network.packets.pipe.InvSysConResistance;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiInvSysConnector extends LogisticsBaseGuiScreen {

	private static final String PREFIX = "gui.invsyscon.";

	private int page = 0;
	private final List<ItemIdentifierStack> _allItems = new ArrayList<ItemIdentifierStack>();
	private final PipeItemsInvSysConnector pipe;
	private int localresistance;

	public GuiInvSysConnector(EntityPlayer player, PipeItemsInvSysConnector pipe) {
		super(180, 200, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, pipe.inv);

		dummy.addRestrictedSlot(0, pipe.inv, 98, 17, new ISlotCheck() {

			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				if (itemStack == null) {
					return false;
				}
				if (itemStack.getItem() != LogisticsPipes.LogisticsItemCard) {
					return false;
				}
				if (itemStack.getItemDamage() != LogisticsItemCard.FREQ_CARD) {
					return false;
				}
				return true;
			}
		});

		dummy.addNormalSlotsForPlayerInventory(10, 115);

		inventorySlots = dummy;
		this.pipe = pipe;
		localresistance = pipe.resistance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 120, guiTop + 47, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 160, guiTop + 47, 10, 10, ">"));
		buttonList.add(new SmallGuiButton(2, guiLeft + 68, guiTop + 47, 46, 10, StringUtils.translate(GuiInvSysConnector.PREFIX + "Refresh")));
		buttonList.add(new SmallGuiButton(3, guiLeft + 80, guiTop + 35, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(4, guiLeft + 120, guiTop + 35, 10, 10, ">"));
		buttonList.add(new SmallGuiButton(5, guiLeft + 140, guiTop + 35, 30, 10, StringUtils.translate(GuiInvSysConnector.PREFIX + "Save")));
		refreshPacket();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 115);
		mc.fontRenderer.drawString(StringUtils.translate(GuiInvSysConnector.PREFIX + "InventorySystemConnector"), guiLeft + 5, guiTop + 6, 0x404040);
		drawRect(guiLeft + 9, guiTop + 58, guiLeft + 170, guiTop + 112, Color.GREY);
		mc.fontRenderer.drawString(StringUtils.translate(GuiInvSysConnector.PREFIX + "ConnectionCard") + ":", guiLeft + 10, guiTop + 21, 0x404040);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 97, guiTop + 16);
		mc.fontRenderer.drawString(StringUtils.translate(GuiInvSysConnector.PREFIX + "Waitingfor") + ":", guiLeft + 10, guiTop + 48, 0x404040);
		mc.fontRenderer.drawString((page + 1) + "/" + maxPage(), guiLeft + 136, guiTop + 49, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiInvSysConnector.PREFIX + "Resistance") + ":", guiLeft + 10, guiTop + 35, 0x404040);
		mc.fontRenderer.drawString(Integer.toString(localresistance), guiLeft + 105 - (mc.fontRenderer.getStringWidth(Integer.toString(localresistance)) / 2), guiTop + 37, 0x404040);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(_allItems, null, page, 9, 59, 9, 27, 18, 18, 100.0F, DisplayAmount.ALWAYS);

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
			int y = 59 + 18 * row + guiTop;

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
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == 0) {
			pageDown();
		} else if (button.id == 1) {
			pageUp();
		} else if (button.id == 2) {
			refreshPacket();
		} else if (button.id == 3) {
			for (int i = 0; i < (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 10 : 1); i++) {
				if (localresistance > 0) {
					localresistance--;
				}
			}
		} else if (button.id == 4) {
			for (int i = 0; i < (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 10 : 1); i++) {
				localresistance++;
			}
		} else if (button.id == 5) {
			pipe.resistance = localresistance;
			MainProxy.sendPacketToServer(PacketHandler.getPacket(InvSysConResistance.class).setInteger(pipe.resistance).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
		}
	}

	public void handleContentAnswer(Collection<ItemIdentifierStack> allItems) {
		_allItems.clear();
		_allItems.addAll(allItems);
	}

	public void handleResistanceAnswer(int resistance) {
		localresistance = resistance;
	}
}
