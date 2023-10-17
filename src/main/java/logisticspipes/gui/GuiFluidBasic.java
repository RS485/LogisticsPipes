package logisticspipes.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiFluidBasic extends LogisticsBaseGuiScreen {

	public GuiFluidBasic(EntityPlayer player, IInventory inventory) {
		super(180, 130, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, inventory);
		dummy.addFluidSlot(0, inventory, 28, 13);
		dummy.addNormalSlotsForPlayerInventory(10, 45);
		inventorySlots = dummy;
	}

	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 45);
		GuiGraphics.drawSlotBackground(mc, guiLeft + 27, guiTop + 12);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if (inventorySlots.getSlot(0).getStack() == null) {
			mc.fontRenderer.drawString(TextUtil.translate("gui.fluidbasic.Empty"), 50, 18, 0x404040);
		} else {
			mc.fontRenderer.drawString(ItemIdentifier.get(inventorySlots.getSlot(0).getStack()).getFriendlyName(), 50, 18, 0x404040);
		}
	}
}
