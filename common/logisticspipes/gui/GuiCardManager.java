package logisticspipes.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.utils.CardManagmentInventory;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiCardManager extends LogisticsBaseGuiScreen {

	public GuiCardManager(EntityPlayer player) {
		super(180, 180, 0, 0);
		final CardManagmentInventory Cinv = new CardManagmentInventory();
		DummyContainer dummy = new DummyContainer(player, Cinv, new IGuiOpenControler() {

			@Override
			public void guiOpenedByPlayer(EntityPlayer player) {}

			@Override
			public void guiClosedByPlayer(EntityPlayer player) {
				Cinv.close(player, (int) player.posX, (int) player.posY, (int) player.posZ);
			}
		});
		dummy.addRestrictedSlot(0, Cinv, 21, 21, LogisticsPipes.ModuleItem);
		dummy.addRestrictedSlot(1, Cinv, 61, 21, LogisticsPipes.ModuleItem);
		dummy.addRestrictedSlot(2, Cinv, 41, 58, new ISlotCheck() {

			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				return false;
			}
		});
		dummy.addRestrictedSlot(3, Cinv, 121, 39, LogisticsPipes.LogisticsItemCard);
		for (int i = 4; i < 7; i++) {
			dummy.addColorSlot(i, Cinv, 101, 21 + (i - 4) * 18);
		}
		for (int i = 7; i < 10; i++) {
			dummy.addColorSlot(i, Cinv, 141, 21 + (i - 7) * 18);
		}
		dummy.addNormalSlotsForPlayerInventory(10, 95);
		inventorySlots = dummy;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int j, int k) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, bottom - 85);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 20, guiTop + 20);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 60, guiTop + 20);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 40, guiTop + 57);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 120, guiTop + 38);
		drawRect(guiLeft + 38, guiTop + 27, guiLeft + 60, guiTop + 31, Color.DARKER_GREY);
		drawRect(guiLeft + 47, guiTop + 31, guiLeft + 51, guiTop + 57, Color.DARKER_GREY);
		for (int i = 0; i < 3; i++) {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 100, guiTop + 20 + i * 18);
		}
		for (int i = 0; i < 3; i++) {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 140, guiTop + 20 + i * 18);
		}
	}
}
