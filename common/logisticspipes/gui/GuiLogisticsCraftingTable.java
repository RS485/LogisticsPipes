package logisticspipes.gui;

import java.util.Arrays;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.CraftingCycleRecipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

public class GuiLogisticsCraftingTable extends LogisticsBaseGuiScreen {

	public LogisticsCraftingTableTileEntity _crafter;

	private int fuzzyPanelSelection = -1;
	private int fuzzyPanelHover = -1;
	private int fuzzyPanelHoverTime = 0;

	private GuiButton[] sycleButtons = new GuiButton[2];

	public GuiLogisticsCraftingTable(EntityPlayer player, LogisticsCraftingTableTileEntity crafter) {
		super(176, 218, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, crafter.matrix);
		dummy.guiHolderForJEI = this;

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (crafter.isFuzzy()) {
					dummy.addFuzzyDummySlot(y * 3 + x, 35 + x * 18, 10 + y * 18, crafter.inputFuzzy(y * 3 + x));
				} else {
					dummy.addDummySlot(y * 3 + x, 35 + x * 18, 10 + y * 18);
				}
			}
		}
		if (crafter.isFuzzy()) {
			dummy.addFuzzyUnmodifiableSlot(0, crafter.resultInv, 125, 28, crafter.outputFuzzy());
		} else {
			dummy.addUnmodifiableSlot(0, crafter.resultInv, 125, 28);
		}
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 9; x++) {
				dummy.addNormalSlot(y * 9 + x, crafter.inv, 8 + x * 18, 80 + y * 18);
			}
		}
		dummy.addNormalSlotsForPlayerInventory(8, 135);
		inventorySlots = dummy;
		_crafter = crafter;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		(sycleButtons[0] = addButton(new SmallGuiButton(0, guiLeft + 144, guiTop + 25, 15, 10, "/\\"))).visible = false;
		(sycleButtons[1] = addButton(new SmallGuiButton(1, guiLeft + 144, guiTop + 37, 15, 10, "\\/"))).visible = false;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float fA, int iA, int jA) {
		for (GuiButton sycleButton : sycleButtons) {
			sycleButton.visible = _crafter.targetType != null;
		}
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + 34 + x * 18, guiTop + 9 + y * 18);
			}
		}
		GuiGraphics.drawSlotBackground(mc, guiLeft + 124, guiTop + 27);
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 2; y++) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + 7 + x * 18, guiTop + 79 + y * 18);
			}
		}
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 8, guiTop + 135);

		ItemIdentifierStack[] items = new ItemIdentifierStack[9];
		for (int i = 0; i < 9; i++) {
			if (_crafter.matrix.getIDStackInSlot(i) != null) {
				items[i] = _crafter.matrix.getIDStackInSlot(i);
			}
		}

		ItemStackRenderer.renderItemIdentifierStackListIntoGui(Arrays.asList(items), null, 0, guiLeft + 8, guiTop + 79, 9, 9, 18, 18, 0.0F, DisplayAmount.NEVER);

		GlStateManager.translate(0, 0, 200F);
		for (int a = 0; a < 9; a++) {
			Gui.drawRect(guiLeft + 8 + (a * 18), guiTop + 80, guiLeft + 24 + (a * 18), guiTop + 96, 0xc08b8b8b);
		}
		GlStateManager.translate(0, 0, -200F);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0 || button.id == 1) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingCycleRecipe.class).setDown(button.id == 1).setTilePos(_crafter));
		}
	}

	private boolean isMouseInFuzzyPanel(int mx, int my) {
		if (fuzzyPanelSelection == -1) {
			return false;
		}
		int posX = -60;
		int posY = 0;
		return mx >= posX && my >= posY && mx <= posX + 60 && my <= posY + 52;
	}
}
